package edu.bu.fdb;

public class ServiceTest {
    public static void main(String[] args) {
        FDBService.UploadAll(".\\src\\test\\files");
        FDBService.DownloadAFile("NASAs Webb Telescope.docx");
        FDBService.ClearAll(".\\src\\test\\files");
    }
}
