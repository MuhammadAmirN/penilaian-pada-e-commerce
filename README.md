# Aplikasi Penilaian Produk E-Commerce (CLI)

Aplikasi berbasis *Command Line Interface* (CLI) yang ditulis menggunakan bahasa Java untuk memenuhi tugas Ujian Tengah Semester (UTS). Aplikasi ini berfungsi untuk melakukan penilaian kelayakan kualitas produk e-commerce berdasarkan rumus kalkulasi skor dan analisis sentimen teks review secara sederhana.

## 📌 Fitur Utama
* **Manajemen Sesi Pengguna:** Mencatat data identitas mahasiswa (No. Urut, NIM, dan Nama) di awal sesi program.
* **Sistem Menu Interaktif:** 1. **Input Data Produk:** Mengkalkulasi skor akhir produk berdasarkan variabel rating, panjang ulasan, dan harga produk.
  2. **Hitung dan Review Produk:** Menganalisis tingkat kepositifan ulasan (*review sentiment breakdown*) berdasarkan kombinasi rating dan jumlah karakter teks ulasan.
  3. **Keluar:** Menutup sesi aplikasi secara aman.
* **Formatting Desimal:** Menggunakan `DecimalFormat` untuk memastikan skor akhir ditampilkan rapi dengan maksimal 2 angka di belakang koma.

## 📐 Rumus & Logika Bisnis

### 1. Kalkulasi Skor Produk (Menu 1)
Skor akhir produk dihitung menggunakan formula:
$$\text{Skor} = (\text{Rating} \times 20) + (\text{Panjang Review} \times 0.1) - \left(\frac{\text{Harga}}{200000}\right)$$

*Jika hasil akhir menghasilkan angka negatif, sistem otomatis membulatkannya menjadi `0`.*

### Threshold Kualitas Produk:
| Range Skor | Kategori Kualitas |
| :--- | :--- |
| $\ge 100$ | Produk Sangat Baik |
| $\ge 75$ | Produk Baik |
| $\ge 50$ | Produk Cukup |
| $\ge 25$ | Produk Buruk |
| $< 25$ | Produk Sangat Buruk |

---

### 2. Klasifikasi Analisis Ulasan (Menu 2)
Penentuan bobot teks ulasan dipengaruhi oleh panjang karakter ulasan (*string length*):
* **Rating $\ge$ 4:** `Review Sangat Positif` (>20 karakter) atau `Review Positif` ($\le$20 karakter).
* **Rating = 3:** `Review Cukup Positif` (>25 karakter) atau `Review Netral` ($\le$25 karakter).
* **Rating $<$ 3:** `Review Kurang Baik` (>30 karakter) atau `Review Negatif` ($\le$30 karakter).

---

## 💻 Prasyarat Menjalankan Aplikasi
* **Java Development Kit (JDK):** Versi 8 atau yang lebih baru.
* **IDE / Terminal:** NetBeans, IntelliJ IDEA, VS Code, atau Command Prompt/Terminal bawaan OS.

## 🚀 Cara Menjalankan Via Terminal
1. Buka terminal pada direktori tempat file `uts.java` berada.
2. Lakukan kompilasi file Java:
   ```bash
   javac uts.java
