package uts;
import java.util.Scanner;
import java.text.DecimalFormat;

public class uts {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in); 
        DecimalFormat decimalFormat = new DecimalFormat("#.##");

        System.out.println("====================================================================");
        System.out.println("Selamat datang di Aplikasi Penilaian Produk E-Commerce!");
        System.out.println("====================================================================");

        System.out.print("No. Urut           : ");
        String nu = scanner.nextLine();

        System.out.print("NIM                : ");
        String nim = scanner.nextLine();

        System.out.print("Nama Mahasiswa     : ");
        String nama = scanner.nextLine();

        // Validasi input menu yang mau dipilih
        System.out.println("\n____________________MENU Pilihan____________________");
        System.out.println("1. Input Data Produk");
        System.out.println("2. Hitung dan review produk");
        System.out.println("3. Keluar");
        Double menu = scanner.nextDouble();
        scanner.nextLine();

        String np = "";
        Double hp = 0.0;
        Double rp = 0.0;
        String review = "";
        double skor = 0.0;
        String Kualitas = "";

        // menampilkan input data produk pada menu 1
        if (menu == 1) {
            System.out.println("\n>>INPUT DATA PRODUK<<");
            System.out.print("Nama Produk           : ");
            np = scanner.nextLine();

            System.out.print("Harga Produk (Rp)     : ");
            hp = scanner.nextDouble();

            System.out.print("Rating Produk (1-5)   : ");
            rp = scanner.nextDouble();
            scanner.nextLine();

            System.out.print("Review                : ");
            review = scanner.nextLine();

            // Perhitungan skor
            skor = (rp * 20) + (review.length() * 0.1) - (hp / 200000);
            if (skor < 0) skor = 0;

            
            // Penilaian kualitas berdasarkan skor akhir
            if (skor >= 100) {
                Kualitas = "Produk Sangat Baik";
            } else if (skor >= 75) {
                Kualitas = "Produk Baik";
            } else if (skor >= 50) {
                Kualitas = "Produk Cukup";
            } else if (skor >= 25) {
                Kualitas = "Produk Buruk";
            } else {
                Kualitas = "Produk Sangat Buruk";
            }

        // menampilkan analisis review produk pada menu 2
        } else if (menu == 2) {
            System.out.println("\n>> ANALISIS REVIEW PRODUK <<");

            System.out.print("Masukkan rating produk (1-5): ");
            int rating = scanner.nextInt();
            scanner.nextLine();

            System.out.print("Masukkan isi review produk: ");
            review = scanner.nextLine();

            String analisis;
              if (rating >= 4) {
                  if (review.length() > 20) {
                      analisis = "Review Sangat Positif";
                  } else {
                      analisis = "Review Positif";
                  }
              } else if (rating == 3) {
                  if (review.length() > 25) {
                      analisis = "Review Cukup Positif";
                  } else {
                      analisis = "Review Netral";
                  }
              } else {
                  if (review.length() > 30) {
                      analisis = "Review Kurang Baik";
                  } else {
                      analisis = "Review Negatif";
                  }
              }
              
            System.out.println("\n--- HASIL ANALISIS ---");
            System.out.println("Rating        : " + rating);
            System.out.println("Panjang Review: " + review.length() + " karakter");
            System.out.println("Analisis      : " + analisis);

        //menampilkan pilihan pada menu 3 atau keluar
        } else if (menu == 3) {
            System.out.println("\nTerima kasih telah menggunakan aplikasi ini!");
        } else {
            System.out.println("\nPilihan tidak valid. Silakan coba lagi.");
        }

        scanner.close();

        // Tampilkan hasil akhir hanya jika menu 1
        if (menu == 1) {
            System.out.println("No. Urut              : " + nu);
            System.out.println("NIM                   : " + nim);
            System.out.println("Nama Mahasiswa        : " + nama);
            System.out.println("\n>>HASIL PENILAIAN PRODUK<<");
            System.out.println("Nama Produk           : " + np);
            System.out.println("Harga Produk (Rp)     : " + hp);
            System.out.println("Rating Produk (1-5)   : " + rp);
            System.out.println("Review                : " + review);
            System.out.println("Skor akhir            : " + decimalFormat.format(skor));
            System.out.println("Kualitas Produk       : " + Kualitas);
        }
    }
}
