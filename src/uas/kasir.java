/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package uas;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import java.awt.event.KeyEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import uas.Koneksi; 
import uas.previewkasir; 
 
/**
 *
 * @author ACER
 */
public class kasir extends javax.swing.JFrame {

private Connection con;
    private Statement stat;
    private int diskonYangDiterapkan = 0;
    private ResultSet rs;
    private String sql;
    private DefaultTableModel modelStruk;
    private int totalHarga; // Variabel untuk menyimpan total harga
    private DecimalFormat kursIndonesia;
    int selectedRow = -1;
    /**
     * Creates new form uas1
     */
     
    public kasir() {  
       
        initComponents();
  
        DecimalFormatSymbols formatRp = new DecimalFormatSymbols();
        formatRp.setCurrencySymbol("Rp. ");
        formatRp.setMonetaryDecimalSeparator(',');
        formatRp.setGroupingSeparator('.');
        kursIndonesia = new DecimalFormat("'Rp. '#,##0", formatRp);
   
        modelStruk = new DefaultTableModel(
                new Object[][]{},
                new String[]{
                    "ID Barang", "Nama Barang", "Harga", "Jumlah", "Subtotal"
                }
        );
        jTable1.setModel(modelStruk);

        tglsekarang(); 
        txttanggal.setEditable(false); 
        txttotal.setEditable(false); 
        txtkembalian.setEditable(false); 
        txttotalharga.setEditable(false); 

        txttotal.setText(kursIndonesia.format(0));
        txtbayar.setText("0");
        txtkembalian.setText(kursIndonesia.format(0));
        

        otomatis_no_transaksi(); 
        setupListeners();
        clearForm(); 

        txtbayar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                hitungKembalian();
            }

            public void keyTyped(java.awt.event.KeyEvent evt) {
                char c = evt.getKeyChar();
                if (!Character.isDigit(c) && c != KeyEvent.VK_BACK_SPACE && c != KeyEvent.VK_DELETE) {
                    evt.consume(); 
                }
            }
        });

        txtharga.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyTyped(java.awt.event.KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isDigit(c) && c != KeyEvent.VK_BACK_SPACE && c != KeyEvent.VK_DELETE) {
                    e.consume();
                }
            }
        });

        txtjumlah.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyTyped(java.awt.event.KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isDigit(c) && c != KeyEvent.VK_BACK_SPACE && c != KeyEvent.VK_DELETE) {
                    e.consume();
                }
            }
        });
    }

     private void otomatis_no_transaksi() {
        try {
            Connection currentCon = Koneksi.getKoneksi(); // Get connection
            String sql = "SELECT MAX(RIGHT(no_transaksi, 4)) AS no_terakhir FROM transaksi";
            PreparedStatement pst = currentCon.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                String noTerakhir = rs.getString("no_terakhir");
                if (noTerakhir != null) {
                    int no = Integer.parseInt(noTerakhir) + 1;
                    String formattedNo = String.format("UDB%04d", no);
                    txtnotransaksi.setText(formattedNo);
                } else {
                    txtnotransaksi.setText("UDB0001");
                }
            }
            rs.close();
            pst.close();
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error generating transaction number: " + e.getMessage(), "Error Auto No. Transaksi", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    // Method to set current date to txttanggal
    private void tglsekarang() {
        Date now = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMMM yyyy");
        txttanggal.setText(simpleDateFormat.format(now));
        txttanggal.setEditable(false); // Make tanggal uneditable
    }  
    
    // Metode untuk mengosongkan semua field
    private void clearForm() {
        txtnotransaksi.setText("");
        tglsekarang(); // Set current date
        txtid.setText("");
        txtnama.setText("");
        txtidbarang.setText("");
        txtnamabarang.setText("");
        txtharga.setText("");
        txtjumlah.setText("");
        txttotal.setText(kursIndonesia.format(0));
        txttotalharga.setText("0"); // This field should hold plain number
        txtbayar.setText("");
        txtkembalian.setText(kursIndonesia.format(0));
        modelStruk.setRowCount(0); // Kosongkan tabel
        totalHarga = 0; // Reset total harga
        diskonYangDiterapkan = 0;
        
        if (rb2 != null){
            rb2.setSelected(true);
            hitungTotal();
        }
        
    }

    
 private void simpanTransaksi() {
    if (modelStruk.getRowCount() == 0) {
        JOptionPane.showMessageDialog(this, "Tidak ada item dalam transaksi untuk disimpan.", "Peringatan", JOptionPane.WARNING_MESSAGE);
        return;
    }

    if (txtid.getText().trim().isEmpty()) {
        JOptionPane.showMessageDialog(this, "ID Customer tidak boleh kosong.", "Validasi Input", JOptionPane.WARNING_MESSAGE);
        txtid.requestFocus();
        return;
    }

    if (txtnama.getText().trim().isEmpty()) {
        JOptionPane.showMessageDialog(this, "Nama Customer tidak boleh kosong.", "Validasi Input", JOptionPane.WARNING_MESSAGE);
        txtnama.requestFocus();
        return;
    }

    Connection currentCon = null;
    try {
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String tanggalUntukSQL = dateFormat.format(now);

        currentCon = Koneksi.getKoneksi();
        currentCon.setAutoCommit(false);

        String sqlTransaksi = "INSERT INTO transaksi (no_transaksi, tanggal, id_customer, nama_customer, total_bayar, bayar, kembalian) VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement pst = currentCon.prepareStatement(sqlTransaksi);

        pst.setString(1, txtnotransaksi.getText());
        pst.setString(2, tanggalUntukSQL);
        pst.setString(3, txtid.getText());
        pst.setString(4, txtnama.getText());

        int totalBayar = Integer.parseInt(txttotalharga.getText().replaceAll("[^0-9]", ""));
        int bayar = Integer.parseInt(txtbayar.getText().replaceAll("[^0-9]", ""));

        if (bayar < totalBayar) {
            JOptionPane.showMessageDialog(this, "Jumlah bayar kurang dari total harga.", "Validasi Pembayaran", JOptionPane.WARNING_MESSAGE);
            txtbayar.requestFocus();
            currentCon.rollback();
            return;
        }

        int kembalian = bayar - totalBayar;
        pst.setInt(5, totalBayar);
        pst.setInt(6, bayar);
        pst.setInt(7, kembalian);
        pst.executeUpdate();
        pst.close();
        
        String sqlDetail = "INSERT INTO detail_transaksi (no_transaksi, id_barang, nama_barang, harga, jumlah, subtotal) VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement pst2 = currentCon.prepareStatement(sqlDetail);

        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        for (int i = 0; i < model.getRowCount(); i++) {
            pst2.setString(1, txtnotransaksi.getText());
            pst2.setString(2, model.getValueAt(i, 0).toString());
            pst2.setString(3, model.getValueAt(i, 1).toString());
            pst2.setInt(4, Integer.parseInt(model.getValueAt(i, 2).toString()));
            pst2.setInt(5, Integer.parseInt(model.getValueAt(i, 3).toString()));
            pst2.setInt(6, Integer.parseInt(model.getValueAt(i, 4).toString()));
            pst2.addBatch();
        }
        pst2.executeBatch();
        pst2.close();

        currentCon.commit();
        JOptionPane.showMessageDialog(null, "Transaksi berhasil disimpan!", "Info", JOptionPane.INFORMATION_MESSAGE);

    } catch (SQLException | NumberFormatException e) {
        try {
            if (currentCon != null) currentCon.rollback();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        JOptionPane.showMessageDialog(null, "Terjadi kesalahan saat menyimpan transaksi: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    } finally {
        try {
            if (currentCon != null && !currentCon.isClosed()) {
                currentCon.setAutoCommit(true);
                currentCon.close();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
     private void hitungTotal() {
         int tempTotalHarga = 0;
        diskonYangDiterapkan = 0;
        for(int i = 0; i < modelStruk.getRowCount(); i++){
            tempTotalHarga += Integer.parseInt(modelStruk.getValueAt(i,4).toString());
        }
        if (rb1.isSelected() && tempTotalHarga >= 500000){
            diskonYangDiterapkan = 100000;
            totalHarga = Math.max(0,tempTotalHarga - diskonYangDiterapkan);
            
            JOptionPane.showMessageDialog(this, "Diskon Rp. " + kursIndonesia.format(diskonYangDiterapkan) + "(Voucer) diterapkan!", "Info Diskon", JOptionPane.INFORMATION_MESSAGE);
        } else {
            totalHarga = tempTotalHarga;
        }
        txttotal.setText(kursIndonesia.format(totalHarga));
        txttotalharga.setText(String.valueOf(totalHarga));
        hitungKembalian();
    }
     
private void hitungKembalian() {
    try {
        String totalText = txttotalharga.getText().replace("Rp", "").replace(".", "").replace(",", "").trim();
        int total = totalText.isEmpty() ? 0 : Integer.parseInt(totalText);
        String bayarText = txtbayar.getText().replaceAll("[^0-9]", "");
        int bayar = bayarText.isEmpty() ? 0 : Integer.parseInt(bayarText);
        int kembalian = bayar - total;
        txtkembalian.setText(kursIndonesia.format(kembalian));
    } catch (NumberFormatException e) {
        txtkembalian.setText(kursIndonesia.format(0));
    }
}

   
     private void setupListeners() {
        txtjumlah.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                calculateCurrentItemSubtotal();
            }
        });
        txtjumlah.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                calculateCurrentItemSubtotal();
            }
        });
        txtbayar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hitungKembalian();
            }
        });
        txtbayar.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                hitungKembalian();
            }
        });
        jTable1.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting() && jTable1.getSelectedRow() != -1) {
                    int selectedRow = jTable1.getSelectedRow();
                    txtidbarang.setText(modelStruk.getValueAt(selectedRow, 0).toString());
                    txtnamabarang.setText(modelStruk.getValueAt(selectedRow, 1).toString());
                    txtharga.setText(modelStruk.getValueAt(selectedRow, 2).toString());
                    txtjumlah.setText(modelStruk.getValueAt(selectedRow, 3).toString());
                }
            }
        });
    }
        
     private void calculateCurrentItemSubtotal() {
        try {
            if (txtharga.getText().isEmpty() || txtjumlah.getText().isEmpty()) {
                return; 
            }
            int harga = Integer.parseInt(txtharga.getText());
            int jumlah = Integer.parseInt(txtjumlah.getText());
            int subtotal = harga * jumlah;
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Harga atau jumlah harus berupa angka yang valid.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jPanel4 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        btambah = new javax.swing.JButton();
        bhapus = new javax.swing.JButton();
        bkeluar = new javax.swing.JButton();
        bsimpan = new javax.swing.JButton();
        bedit = new javax.swing.JButton();
        txttotal = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        txttotalharga = new javax.swing.JTextField();
        txtbayar = new javax.swing.JTextField();
        txtkembalian = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        txtnotransaksi = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        txtid = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        txtnama = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        txtidbarang = new javax.swing.JTextField();
        txtnamabarang = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        txtharga = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        txtjumlah = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        txtprint = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        bbaru = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        txttanggal = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        rb1 = new javax.swing.JRadioButton();
        rb2 = new javax.swing.JRadioButton();

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(204, 204, 204));

        btambah.setBackground(new java.awt.Color(153, 153, 153));
        btambah.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btambah.setText("TAMBAH");
        btambah.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btambahActionPerformed(evt);
            }
        });

        bhapus.setBackground(new java.awt.Color(153, 153, 153));
        bhapus.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        bhapus.setText("HAPUS");
        bhapus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bhapusActionPerformed(evt);
            }
        });

        bkeluar.setBackground(new java.awt.Color(153, 153, 153));
        bkeluar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        bkeluar.setText("KELUAR");
        bkeluar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bkeluarActionPerformed(evt);
            }
        });

        bsimpan.setBackground(new java.awt.Color(153, 153, 153));
        bsimpan.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        bsimpan.setText("SIMPAN");
        bsimpan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bsimpanActionPerformed(evt);
            }
        });

        bedit.setBackground(new java.awt.Color(153, 153, 153));
        bedit.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        bedit.setText("EDIT");
        bedit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                beditActionPerformed(evt);
            }
        });

        txttotal.setBackground(new java.awt.Color(153, 255, 153));
        txttotal.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        txttotal.setText("Rp. 0");
        txttotal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txttotalActionPerformed(evt);
            }
        });

        jLabel10.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel10.setText("Total ");

        jLabel11.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel11.setText("Bayar");

        txttotalharga.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N

        txtbayar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N

        txtkembalian.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        txtkembalian.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtkembalianActionPerformed(evt);
            }
        });

        jLabel12.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel12.setText("Kembalian");

        txtnotransaksi.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        txtnotransaksi.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                txtnotransaksiMouseClicked(evt);
            }
        });
        txtnotransaksi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtnotransaksiActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel2.setText("No Transaksi");

        txtid.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        txtid.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtidActionPerformed(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel3.setText("ID Customer");

        txtnama.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        txtnama.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtnamaActionPerformed(evt);
            }
        });
        txtnama.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtnamaKeyReleased(evt);
            }
        });

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel4.setText("Nama Costumer");

        txtidbarang.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N

        txtnamabarang.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel6.setText("ID Barang");

        txtharga.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        txtharga.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txthargaActionPerformed(evt);
            }
        });

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel7.setText("Nama Barang");

        txtjumlah.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        txtjumlah.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtjumlahActionPerformed(evt);
            }
        });

        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel8.setText("Harga");

        jLabel9.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel9.setText("Jumlah");

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel1.setText("KASIR SEDERHANA");

        txtprint.setBackground(new java.awt.Color(255, 0, 51));
        txtprint.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        txtprint.setText("PRINT");
        txtprint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtprintActionPerformed(evt);
            }
        });

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jTable1.setShowGrid(true);
        jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable1MouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(jTable1);

        bbaru.setBackground(new java.awt.Color(153, 153, 153));
        bbaru.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        bbaru.setText("BARU");
        bbaru.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bbaruActionPerformed(evt);
            }
        });

        jPanel2.setBackground(new java.awt.Color(204, 204, 204));

        txttanggal.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N

        jLabel13.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel13.setText("TANGGAL");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(9, Short.MAX_VALUE)
                .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(txttanggal, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txttanggal, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3.setBackground(new java.awt.Color(204, 204, 204));

        buttonGroup1.add(rb1);
        rb1.setText("Dapat Vocher");
        rb1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rb1ActionPerformed(evt);
            }
        });

        buttonGroup1.add(rb2);
        rb2.setText("Tidak Dapat");
        rb2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rb2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(rb2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(rb1, javax.swing.GroupLayout.DEFAULT_SIZE, 109, Short.MAX_VALUE))
                .addContainerGap(20, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(rb1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 18, Short.MAX_VALUE)
                .addComponent(rb2))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 789, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(bsimpan, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(txttotal, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(txtprint, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel10)
                                    .addComponent(jLabel11)
                                    .addComponent(jLabel12))
                                .addGap(18, 18, 18)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(txttotalharga, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 298, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtbayar, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 298, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtkembalian, javax.swing.GroupLayout.PREFERRED_SIZE, 298, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(bhapus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btambah, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(bbaru, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(bkeluar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(bedit, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(txtnama, javax.swing.GroupLayout.PREFERRED_SIZE, 317, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(225, 225, 225))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(jLabel3))
                                        .addGap(40, 40, 40)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(txtid)
                                            .addComponent(txtnotransaksi, javax.swing.GroupLayout.PREFERRED_SIZE, 317, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 178, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addGap(8, 8, 8)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(127, 127, 127))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtidbarang)
                            .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtnamabarang, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(txtharga, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtjumlah, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(9, 9, 9)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtnotransaksi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtid, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3)))
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(9, 9, 9)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtnama, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addGap(38, 38, 38)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel7)
                            .addComponent(jLabel6)
                            .addComponent(jLabel8)
                            .addComponent(jLabel9))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtidbarang, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtnamabarang)
                            .addComponent(txtharga)
                            .addComponent(txtjumlah, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(34, 34, 34))
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(bbaru, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btambah, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(bhapus, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 181, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txttotal, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(bsimpan, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtprint, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel10)
                                    .addComponent(txttotalharga, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel11)
                                    .addComponent(txtbayar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel12)
                                    .addComponent(txtkembalian, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(4, 4, 4)
                        .addComponent(bedit, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(bkeluar, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 966, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btambahActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btambahActionPerformed
       // TODO add your handling code here:
        // Validasi input
        if (txtidbarang.getText().isEmpty() || txtnamabarang.getText().isEmpty()
                || txtharga.getText().isEmpty() || txtjumlah.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Semua field barang harus diisi.", "Validasi Input", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            String idBarang = txtidbarang.getText();
            String namaBarang = txtnamabarang.getText();
            int harga = Integer.parseInt(txtharga.getText());
            int jumlah = Integer.parseInt(txtjumlah.getText());
            int subtotal = harga * jumlah;

            // Tambahkan data ke model tabel
            modelStruk.addRow(new Object[]{idBarang, namaBarang, harga, jumlah, subtotal});

            // Hitung ulang total harga
            hitungTotal();

            // Kosongkan field input barang setelah ditambahkan
            txtidbarang.setText("");
            txtnamabarang.setText("");
            txtharga.setText("");
            txtjumlah.setText("");
            txtidbarang.requestFocus(); // Set fokus kembali ke ID Barang

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Harga dan Jumlah harus berupa angka yang valid.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btambahActionPerformed

    private void bhapusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bhapusActionPerformed
     // TODO add your handling code here:
        int selectedRow = jTable1.getSelectedRow();
        if (selectedRow != -1) { // Pastikan ada baris yang dipilih
            modelStruk.removeRow(selectedRow);
            hitungTotal(); // Hitung ulang total setelah menghapus item
            // Clear item input fields
            txtidbarang.setText("");
            txtnamabarang.setText("");
            txtharga.setText("");
            txtjumlah.setText("");
        } else {
            JOptionPane.showMessageDialog(this, "Pilih baris yang ingin dihapus dari tabel.", "Peringatan", JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_bhapusActionPerformed

    private void bkeluarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bkeluarActionPerformed
   // TODO add your handling code here:
        int confirm = JOptionPane.showConfirmDialog(this, "Anda yakin ingin keluar?", "Konfirmasi Keluar", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }//GEN-LAST:event_bkeluarActionPerformed

    private void bsimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bsimpanActionPerformed
       // TODO add your handling code here:
        simpanTransaksi();
    }//GEN-LAST:event_bsimpanActionPerformed

    private void beditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_beditActionPerformed
     int currentRow = jTable1.getSelectedRow(); // Ambil baris yang sedang dipilih
    if (currentRow != -1) { // Pastikan ada baris yang dipilih
        // Validasi input
        if (txtidbarang.getText().isEmpty() || txtnamabarang.getText().isEmpty()
                || txtharga.getText().isEmpty() || txtjumlah.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Semua field barang harus diisi untuk mengedit.", "Validasi Input", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            String idBarang = txtidbarang.getText();
            String namaBarang = txtnamabarang.getText();
            int harga = Integer.parseInt(txtharga.getText());
            int jumlah = Integer.parseInt(txtjumlah.getText());
            int subtotal = harga * jumlah;

            // Update data di model tabel menggunakan currentRow
            modelStruk.setValueAt(idBarang, currentRow, 0);
            modelStruk.setValueAt(namaBarang, currentRow, 1);
            modelStruk.setValueAt(harga, currentRow, 2);
            modelStruk.setValueAt(jumlah, currentRow, 3);
            modelStruk.setValueAt(subtotal, currentRow, 4);

            // Hitung ulang total harga
            hitungTotal();

            // Kosongkan field input barang setelah diedit
            txtidbarang.setText("");
            txtnamabarang.setText("");
            txtharga.setText("");
            txtjumlah.setText("");
            txtidbarang.requestFocus();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Harga dan Jumlah harus berupa angka yang valid.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    } else {
        JOptionPane.showMessageDialog(this, "Pilih baris yang ingin diedit dari tabel.", "Peringatan", JOptionPane.WARNING_MESSAGE);
    }         
    }//GEN-LAST:event_beditActionPerformed

    private void txttotalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txttotalActionPerformed
    // TODO add your handling code here:
    }//GEN-LAST:event_txttotalActionPerformed

    private void txtkembalianActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtkembalianActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtkembalianActionPerformed

    private void txtnotransaksiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtnotransaksiActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtnotransaksiActionPerformed

    private void txtidActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtidActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtidActionPerformed

    private void txtnamaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtnamaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtnamaActionPerformed

    private void txthargaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txthargaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txthargaActionPerformed

    private void txtjumlahActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtjumlahActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtjumlahActionPerformed

    private void txtprintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtprintActionPerformed
generateAndShowStruk();
    }//GEN-LAST:event_txtprintActionPerformed

    private void bbaruActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bbaruActionPerformed
    // BARU Button Action
        clearForm();
        tglsekarang();
        // TODO add your handling code here:
    }//GEN-LAST:event_bbaruActionPerformed

    private void jTable1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable1MouseClicked
      selectedRow = jTable1.getSelectedRow();
    if (selectedRow != -1) {
        txtidbarang.setText(modelStruk.getValueAt(selectedRow, 0).toString());
        txtnamabarang.setText(modelStruk.getValueAt(selectedRow, 1).toString());
        txtharga.setText(modelStruk.getValueAt(selectedRow, 2).toString());
        txtjumlah.setText(modelStruk.getValueAt(selectedRow, 3).toString());
    }    // TODO add your handling code here:
    }//GEN-LAST:event_jTable1MouseClicked

    private void txtnotransaksiMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_txtnotransaksiMouseClicked
  // TODO add your handling code here:
    }//GEN-LAST:event_txtnotransaksiMouseClicked

    private void txtnamaKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtnamaKeyReleased
         // TODO add your handling code here:
    }//GEN-LAST:event_txtnamaKeyReleased

    private void rb1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rb1ActionPerformed
    hitungTotal();        // TODO add your handling code here:
    }//GEN-LAST:event_rb1ActionPerformed

    private void rb2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rb2ActionPerformed
    hitungTotal();        // TODO add your handling code here:
    }//GEN-LAST:event_rb2ActionPerformed
// // Ganti seluruh metode generateAndShowStruk() yang sudah ada di kasir.java dengan kode ini
private void generateAndShowStruk() {
    StringBuilder strukBuilder = new StringBuilder();
    strukBuilder.append("         --- STRUK PEMBELIAN ---\n");
    strukBuilder.append("------------------------------------------\n");
    strukBuilder.append("No Transaksi: ").append(txtnotransaksi.getText()).append("\n");
    strukBuilder.append("Tanggal     : ").append(txttanggal.getText()).append("\n");
    strukBuilder.append("Customer    : ").append(txtnama.getText()).append("\n"); 
    strukBuilder.append("------------------------------------------\n");

    // --- LOOP UNTUK MENAMBAHKAN ITEM BARANG DENGAN FORMAT BARU ---
    for (int i = 0; i < modelStruk.getRowCount(); i++) {
        String namaBarang = modelStruk.getValueAt(i, 1).toString();
        int jumlah = Integer.parseInt(modelStruk.getValueAt(i, 3).toString());
        String hargaSatuanFormatted = kursIndonesia.format(Integer.parseInt(modelStruk.getValueAt(i, 2).toString()));
        String subtotalItemFormatted = kursIndonesia.format(Integer.parseInt(modelStruk.getValueAt(i, 4).toString())); 
        
        strukBuilder.append(namaBarang).append("\n");
        
        // Baris kedua: Jumlah, Harga Satuan (dalam kurung), dan Subtotal Item
        // Format: "   x[Jumlah] ([Harga Satuan]) [Subtotal Item]"
        // Penjelasan format String.format:
        // "   " : 3 spasi indentasi
        // "x%-3d" : "x" diikuti jumlah (rata kiri, lebar minimal 3 karakter untuk angka)
        // "(%-12s)" : Tanda kurung di sekitar harga satuan (rata kiri, lebar minimal 12 karakter untuk string di dalamnya)
        // "%12s\n": Subtotal item (rata kanan, lebar minimal 12 karakter untuk string)
        strukBuilder.append(String.format("   x%-3d (%-12s) %12s\n", 
                                        jumlah, hargaSatuanFormatted, subtotalItemFormatted)); 
    }
    strukBuilder.append("------------------------------------------\n"); 

    // --- Bagian Diskon (tetap sama) ---
    if (diskonYangDiterapkan > 0) {
        int totalSebelumDiskon = totalHarga + diskonYangDiterapkan; 
        strukBuilder.append(String.format("%-30s %10s\n", "Total Belanja Awal:", kursIndonesia.format(totalSebelumDiskon)));
        strukBuilder.append(String.format("%-28s %10s\n", "Diskon Voucher:", "- " + kursIndonesia.format(diskonYangDiterapkan)));
        strukBuilder.append("------------------------------------------\n");
    }

    // --- Bagian Total, Bayar, Kembalian (tetap sama) ---
    strukBuilder.append(String.format("%-30s %10s\n", "TOTAL:", kursIndonesia.format(totalHarga))); 
    strukBuilder.append(String.format("%-30s %10s\n", "BAYAR:", kursIndonesia.format(Integer.parseInt(txtbayar.getText().replaceAll("[^0-9]", "")))));
    strukBuilder.append(String.format("%-30s %10s\n", "KEMBALIAN:", kursIndonesia.format(Integer.parseInt(txtkembalian.getText().replace("Rp", "").replace(".", "").replace(",", "").trim()))));
    strukBuilder.append("\n          TERIMA KASIH TELAH BERBELANJA\n");
    strukBuilder.append("------------------------------------------\n");

    previewkasir previewFrame = new previewkasir(strukBuilder.toString());
    previewFrame.setVisible(true);
}
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(kasir.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(kasir.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(kasir.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(kasir.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new kasir().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bbaru;
    private javax.swing.JButton bedit;
    private javax.swing.JButton bhapus;
    private javax.swing.JButton bkeluar;
    private javax.swing.JButton bsimpan;
    private javax.swing.JButton btambah;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JRadioButton rb1;
    private javax.swing.JRadioButton rb2;
    private javax.swing.JTextField txtbayar;
    private javax.swing.JTextField txtharga;
    private javax.swing.JTextField txtid;
    private javax.swing.JTextField txtidbarang;
    private javax.swing.JTextField txtjumlah;
    private javax.swing.JTextField txtkembalian;
    private javax.swing.JTextField txtnama;
    private javax.swing.JTextField txtnamabarang;
    private javax.swing.JTextField txtnotransaksi;
    private javax.swing.JButton txtprint;
    private javax.swing.JTextField txttanggal;
    private javax.swing.JTextField txttotal;
    private javax.swing.JTextField txttotalharga;
    // End of variables declaration//GEN-END:variables
}
