package id.restabayu.eventsadd;

public class ClassifiedEvent {
    private String eventId;
    private String kategori;
    private String tempat;
    private String deskripsi;
    private String tanggal;
    private String waktu;
    private String selesai;
    private String notif;
    private String nama;

    public ClassifiedEvent(String eventId, String kategori, String tempat, String deskripsi, String tanggal, String waktu, String selesai, String notif, String nama) {
        this.eventId = eventId;
        this.kategori = kategori;
        this.tempat = tempat;
        this.deskripsi = deskripsi;
        this.tanggal = tanggal;
        this.waktu = waktu;
        this.selesai = selesai;
        this.notif = notif;
        this.nama = nama;
    }
    public ClassifiedEvent(){

    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public void setKategori(String kategori) {
        this.kategori = kategori;
    }

    public void setTempat(String tempat) {
        this.tempat = tempat;
    }

    public void setDeskripsi(String deskripsi) {
        this.deskripsi = deskripsi;
    }

    public void setTanggal(String tanggal) {
        this.tanggal = tanggal;
    }

    public void setWaktu(String waktu) {
        this.waktu = waktu;
    }

    public void setSelesai(String selesai) {
        this.selesai = selesai;
    }

    public void setNotif(String notif) {
        this.notif = notif;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getEventId() {
        return eventId;
    }

    public String getKategori() {
        return kategori;
    }

    public String getTempat() {
        return tempat;
    }

    public String getDeskripsi() {
        return deskripsi;
    }

    public String getTanggal() {
        return tanggal;
    }

    public String getWaktu() {
        return waktu;
    }

    public String getSelesai() {
        return selesai;
    }

    public String getNotif() {
        return notif;
    }

    public String getNama() {
        return nama;
    }
}