/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uas;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

public class Koneksi {
    private static Connection koneksi;

    public static Connection getKoneksi() throws SQLException {
    if (koneksi == null || koneksi.isClosed()) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            koneksi = DriverManager.getConnection("jdbc:mysql://localhost:3306/db_kasir", "root", "");
            // Pesan ini akan muncul jika koneksi berhasil
            // JOptionPane.showMessageDialog(null, "Koneksi database berhasil!", "Info", JOptionPane.INFORMATION_MESSAGE);
        } catch (ClassNotFoundException e) {
            // Jangan hanya e.printStackTrace(), tampilkan juga ke user
            JOptionPane.showMessageDialog(null, "Driver MySQL tidak ditemukan: " + e.getMessage(), "Error Koneksi", JOptionPane.ERROR_MESSAGE);
            throw new SQLException("Driver MySQL tidak ditemukan.", e);
        } catch (SQLException e) {
            // Tampilkan pesan error koneksi ke user
            JOptionPane.showMessageDialog(null, "Koneksi database gagal: " + e.getMessage(), "Error Koneksi", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace(); // Cetak stack trace ke konsol untuk debugging
            throw new SQLException("Koneksi database gagal.", e); // Lanjutkan melempar exception
        }
    }
    return koneksi;
}
}
