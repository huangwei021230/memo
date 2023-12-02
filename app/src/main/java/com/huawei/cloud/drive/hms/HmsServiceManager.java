package com.huawei.cloud.drive.hms;

import static com.huawei.cloud.drive.constants.MimeType.mimeType;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.huawei.cloud.base.http.FileContent;
import com.huawei.cloud.base.media.MediaHttpDownloader;
import com.huawei.cloud.base.media.MediaHttpDownloaderProgressListener;
import com.huawei.cloud.base.util.DateTime;
import com.huawei.cloud.base.util.StringUtils;
import com.huawei.cloud.base.util.base64.Base64;
import com.huawei.cloud.drive.log.Logger;
import com.huawei.cloud.drive.task.task.DriveTask;
import com.huawei.cloud.drive.task.task.TaskManager;
import com.huawei.cloud.drive.utils.thumbnail.ThumbnailUtilsImage;
import com.huawei.cloud.services.drive.Drive;
import com.huawei.cloud.services.drive.model.About;
import com.huawei.cloud.services.drive.model.Channel;
import com.huawei.cloud.services.drive.model.Comment;
import com.huawei.cloud.services.drive.model.File;
import com.huawei.cloud.services.drive.model.FileList;
import com.huawei.cloud.services.drive.model.HistoryVersion;
import com.huawei.cloud.services.drive.model.Reply;
import com.huawei.cloud.services.drive.model.StartCursor;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import boogiepop.memo.R;

public class HmsServiceManager {
    public static final String TAG = "HmsServiceManager";
    public static final String FILENAME = "IMG_20190712_155412.jpg";

    public static final String DOCXFILE = "test.docx";

    /**
     * 在线预览/编辑文档的时候，退出webview界面，刷新文件列表界面
     */
    public static final int WEB_VIEW_BACK_REFRESH = 5201;

    public static final long DIRECT_UPLOAD_MAX_SIZE = 20 * 1024 * 1024;

    public static final long DIRECT_DOWNLOAD_MAX_SIZE = 20 * 1024 * 1024;

    // Successful result
    public static final int SUCCESS = 0;

    // Failure result
    public static final int FAIL = 1;

    // Margin space
    public static final int ZOOM_OUT = 30;

    // Main view
    public View mView;

    // Context
    public Context context;

    public HistoryVersion mHistoryVersion;

    public HistoryVersion deleteHistoryVersions;

    // Used to cache metadata information after the folder is created successfully.
    public File mDirectory;

    // Used to cache metadata information after successful file creation
    public File mFile;

    public File mBackupFile;

    // Used to cache metadata information after the Comment is created successfully.
    public Comment mComment;

    //  Used to cache metadata information after the Reply is created successfully.
    public Reply mReply;

    // Used to cache channel token
    public String watchListPageToken;

    public HmsServiceManager(Context context){
        this.context = context;
    }

    public void prepareTestFile() {
        try {
            InputStream in = context.getAssets().open(FILENAME);
            String cachePath = context.getExternalCacheDir().getAbsolutePath();
            FileOutputStream outputStream = new FileOutputStream(new java.io.File(cachePath + "/cache.jpg"));
            byte[] buffer = new byte[1024];
            int byteCount = 0;
            while ((byteCount = in.read(buffer)) != -1) {
                outputStream.write(buffer, 0, byteCount);
            }
            outputStream.flush();
            outputStream.close();
            in.close();

            in = context.getAssets().open(DOCXFILE);
            outputStream = new FileOutputStream(new java.io.File(cachePath + "/test.docx"));
            byte[] buf = new byte[1024];
            while ((byteCount = in.read(buf)) != -1) {
                outputStream.write(buf, 0, byteCount);
            }
            outputStream.flush();
            outputStream.close();
            in.close();

            String BackFileName = "AppDataBackUpFileName.jpg";
            in = context.getAssets().open(FILENAME);
            outputStream = new FileOutputStream(new java.io.File("/sdcard/" + BackFileName));
            while ((byteCount = in.read(buffer)) != -1) {
                outputStream.write(buffer, 0, byteCount);
            }
            outputStream.flush();
            outputStream.close();
            in.close();

            String accountFile = "account.json";
            in = context.getAssets().open(accountFile);
            outputStream = new FileOutputStream(new java.io.File(cachePath +"/"+ accountFile));
            while ((byteCount = in.read(buffer)) != -1) {
                outputStream.write(buffer, 0, byteCount);
            }
            outputStream.flush();
            outputStream.close();
            in.close();
        } catch (IOException e) {
            Logger.e(TAG, "prepare file error, exception: " + e.toString());
            return;
        }
    }
    /**
     * Handle UI refresh message
     */
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            showErrorToast(msg);
        }
    };
    public void showErrorToast(Message msg) {
        if (msg.what == SUCCESS || msg.obj == null) {
            return;
        }
//        Toast.makeText(getActivity(), msg.obj.toString(), Toast.LENGTH_LONG).show();
        Toast.makeText(context, msg.obj.toString(), Toast.LENGTH_LONG).show();
    }
    public Drive buildDrive() {
        Drive service = new Drive.Builder(CredentialManager.getInstance().getCredential(), context).build();
        return service;
    }
    /**
     * Execute the About.get interface test task
     */
    public void executeAboutGet() {
        TaskManager.getInstance().execute(new HmsServiceManager.AboutGetTask());
    }

    /**
     * The About.get interface test task
     */
    public class AboutGetTask extends DriveTask {
        @Override
        public void call() {
            doAbout();
        }
    }

    /**
     * Test the About.get interface
     */
    public void doAbout() {
        try {
            Drive drive = buildDrive();
            Drive.About about = drive.about();
            About response = about.get().set("fields", "*").execute();
            checkUpdateProtocol(response);
            sendHandleMessage(R.id.drive_about_button_get, SUCCESS);
        } catch (Exception e) {
            sendHandleMessage(R.id.drive_about_button_get, FAIL);
            Logger.e(TAG, "getAboutInfo error: " + e.toString());
        }
    }

    /**
     * Determine if you want to pop up the update page
     *
     * @param about Returned response
     */
    public void checkUpdateProtocol(About about) {
        if (about == null) {
            return;
        }
        Log.d(TAG, "checkUpdate: " + about.toString());

        Object updateValue = about.get("needUpdate");
        boolean isNeedUpdate = false;
        if (updateValue instanceof Boolean) {
            isNeedUpdate = (Boolean) updateValue;
        }
        if (!isNeedUpdate) {
            return;
        }

        Object urlValue = about.get("updateUrl");
        String url = "";
        if (urlValue instanceof String) {
            url = (String) urlValue;
        }
        if (TextUtils.isEmpty(url)) {
            return;
        }

        Uri uri = Uri.parse(url);
        if (!"https".equals(uri.getScheme())) {
            return;
        }
//        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        try {
//            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Logger.e(TAG, "Activity Not found");
        }
    }

    public void sendHandleMessage(int buttonId, int result) {
        sendHandleMessage(buttonId, result, null);
    }

    /**
     * Update the button style based on the returned result
     *
     * @param buttonId button id
     * @param result Interface test result 0 success 1 failure
     */
    public void sendHandleMessage(int buttonId, int result, String msg) {
        Message message = handler.obtainMessage();
        message.arg1 = buttonId;
        message.what = result;
        message.obj = msg;
        handler.sendMessage(message);
    }

    /**
     * Execute the Files.list interface test task
     */
    public void executeFilesList() {
        TaskManager.getInstance().execute(new HmsServiceManager.FilesListTask());
    }

    /**
     * The Files.list interface test task
     */
    public class FilesListTask extends DriveTask {
        @Override
        public void call() {
            doFilesList();
        }
    }

    /**
     * Test the Files.list interface
     */
    public void doFilesList() {
        try {
            List<File> folders = getFileList("mimeType = 'application/vnd.huawei-apps.folder'", "fileName", 10, "*");
            Logger.i(TAG, "executeFilesList: directory size =  " + folders.size());
            if (folders.isEmpty()) {
                sendHandleMessage(R.id.drive_files_button_list, SUCCESS);
                return;
            }
            // get child files of a folder
            String directoryId = folders.get(0).getId();

            String queryStr = "'" + directoryId + "' in parentFolder and mimeType != 'application/vnd.huawei-apps.folder'";
            List<File> files = getFileList(queryStr, "fileName", 10, "*");
            Logger.i(TAG, "executeFilesList: files size = " + files.size());
            sendHandleMessage(R.id.drive_files_button_list, SUCCESS);
        } catch (Exception e) {
            sendHandleMessage(R.id.drive_files_button_list, FAIL);
            Logger.e(TAG, "executeFilesList exception: " + e.toString());
        }
    }

    /**
     * Traverse to get all files
     *
     * @param query Query conditions
     * @param orderBy Sort conditions
     * @param pageSize page Size
     * @param fields fields
     */
    public List<File> getFileList(String query, String orderBy, int pageSize, String fields) throws IOException {

        Drive drive = buildDrive();
        Drive.Files.List request = drive.files().list();
        String pageToken = null;
        List<File> fileList = new ArrayList<>();
        do {
            FileList result = request.setQueryParam(query).setOrderBy(orderBy).setPageSize(pageSize).setFields(fields).execute();
            for (File file : result.getFiles()) {
                fileList.add(file);
            }
            pageToken = result.getNextCursor();
            request.setCursor(pageToken);
        } while (!StringUtils.isNullOrEmpty(pageToken));
        Logger.i(TAG, "getFileList: get files counts = " + fileList.size());
        return fileList;
    }

    /**
     * Get parent dir for copy files
     *
     * @param fileList files list
     * @return file ID of parent dir
     */
    public ArrayList<String> getParentsId(FileList fileList) {
        if (fileList == null) {
            return null;
        }
        List<File> files = fileList.getFiles();
        if (files == null || files.size() <= 0) {
            return null;
        }
        int size = files.size();
        File file = files.get(size - 1);
        if (file == null) {
            return null;
        }
        // get the first one for test
        String parentDir = file.getParentFolder().get(0);
        ArrayList<String> list = new ArrayList<>();
        list.add(parentDir);
        return list;
    }

    /**
     * Execute the Files.create interface test task
     */
    public void executeFilesCreate() {
        TaskManager.getInstance().execute(new HmsServiceManager.FilesCreateTask());
    }

    /**
     * Execute the Files.create interface test task
     */
    public class FilesCreateTask extends DriveTask {

        @Override
        public void call() {
            mDirectory = createDirectory();
        }
    }

    /**
     * Create a directory
     */
    public File createDirectory() {
        try {
            Drive drive = buildDrive();
            Map<String, String> appProperties = new HashMap<>();
            appProperties.put("appProperties", "property");
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS");
            String dirName = formatter.format(new Date());
            Logger.i(TAG, "executeFilesCreate: " + dirName);

            File file = new File();
            file.setFileName(dirName).setAppSettings(appProperties).setMimeType("application/vnd.huawei-apps.folder");
            File directory = drive.files().create(file).execute();
            sendHandleMessage(R.id.drive_files_button_create, SUCCESS);
            return directory;
        } catch (IOException e) {
            sendHandleMessage(R.id.drive_files_button_create, FAIL);
            Logger.e(TAG, "createDirectory error: " + e.toString());
            return null;
        }
    }
    public File createDirectoryWithName(String dirName){
        try {
            Drive drive = buildDrive();
            Map<String, String> appProperties = new HashMap<>();
            appProperties.put("appProperties", "property");
            Logger.i(TAG, "executeFilesCreate: " + dirName);

            File file = new File();
            file.setFileName(dirName).setAppSettings(appProperties).setMimeType("application/vnd.huawei-apps.folder");
            File directory = drive.files().create(file).execute();
            sendHandleMessage(R.id.drive_files_button_create, SUCCESS);
            return directory;
        } catch (IOException e) {
            sendHandleMessage(R.id.drive_files_button_create, FAIL);
            Logger.e(TAG, "createDirectory error: " + e.toString());
            return null;
        }
    }
    /**
     * Execute the Files.update interface test task
     */
    public void executeFilesUpdate() {
        TaskManager.getInstance().execute(new HmsServiceManager.FilesUpdateTask());
    }

    /**
     * The Files.create interface test task
     */
    public class FilesUpdateTask extends DriveTask {

        @Override
        public void call() {
            updateFile(mDirectory);
        }
    }

    /**
     * Modify the file (directory) metaData, distinguish whether it is a file or a directory by MIMEType
     *
     * @param file File to be modified (directory)
     */
    public void updateFile(File file) {
        try {
            if (file == null) {
                Logger.e(TAG, "updateFile error, need to create file.");
                sendHandleMessage(R.id.drive_files_button_update, FAIL);
                return;
            }

            Drive drive = buildDrive();
            File updateFile = new File();
            updateFile.setFileName(file.getFileName() + "_update").setMimeType("application/vnd.huawei-apps.folder").setDescription("update folder").setFavorite(true);
            file = drive.files().update(file.getId(), updateFile).execute();

            Logger.i(TAG, "updateFile result: " + file.toString());
            sendHandleMessage(R.id.drive_files_button_update, SUCCESS);
        } catch (Exception e) {
            sendHandleMessage(R.id.drive_files_button_update, FAIL);
            Logger.e(TAG, "updateFile error: " + e.toString());
        }
    }

    /**
     * Execute the Files.create interface test task
     */
    public void executeFilesCreateFile() {
        TaskManager.getInstance().execute(new HmsServiceManager.CreateFileTask());
    }

    /**
     * The Files.create interface test task
     */
    public class CreateFileTask extends DriveTask {

        @Override
        public void call() {
            String fileName = context.getExternalCacheDir().getAbsolutePath() + "/cache.jpg";
            byte[] thumbnailImageBuffer = getThumbnailImage(fileName);
            String type = mimeType(".jpg");
            if (mDirectory == null) {
                String errMsg = "create file error: need to create directory first.";
                Logger.e(TAG, errMsg);
                sendHandleMessage(R.id.drive_files_button_createfile, FAIL, errMsg);
                return;
            }
            createFile(fileName, mDirectory.getId(), thumbnailImageBuffer, type);
        }
    }

    public File createTxTFile(String fileName, File dir) {
        String parentId = dir.getId();
        try {
            File content = new File()
                    .setFileName(fileName)
                    .setMimeType("text/plain") // Set MIME type for txt file
                    .setParentFolder(Collections.singletonList(parentId))
                    .setContentExtras(null); // No thumbnail for text file

            Drive drive = buildDrive();
            Drive.Files.Create request = drive.files().create(content, null); // Pass null as FileContent for empty file

            // default: resume, If the file Size is less than 20M, use directly upload.
            boolean isDirectUpload = false;
            request.getMediaHttpUploader().setDirectUploadEnabled(isDirectUpload);

            mFile = request.execute();
            Logger.i(TAG, "executeFilesCreateFile:" + mFile.toString());
        } catch (Exception e) {
            Logger.e(TAG, "executeFilesCreateFile exception: " + e.toString());
        }
        return mFile;
    }


    /**
     * create a image file by Files.create interface.
     *
     * @param filePath Specifies the file to be uploaded.
     * @param parentId Specifies the directory ID for uploading files
     * @param thumbnailImageBuffer thumbnail Image Data
     * @param thumbnailMimeType image mime type
     */
    public void createFile(String filePath, String parentId, byte[] thumbnailImageBuffer, String thumbnailMimeType) {
        try {
            if (filePath == null) {
                sendHandleMessage(R.id.drive_files_button_createfile, FAIL);
                return;
            }

            java.io.File io = new java.io.File(filePath);
            FileContent fileContent = new FileContent(mimeType(io), io);

            // set thumbnail , If it is not a media file, you do not need a thumbnail.
            File.ContentExtras contentPlus = new File.ContentExtras();
            File.ContentExtras.Thumbnail thumbnail = new File.ContentExtras.Thumbnail();
            thumbnail.setContent(Base64.encodeBase64String(thumbnailImageBuffer));
            thumbnail.setMimeType(thumbnailMimeType);
            contentPlus.setThumbnail(thumbnail);

            File content = new File().setFileName(io.getName()).setMimeType(mimeType(io)).setParentFolder(Collections.singletonList(parentId)).setContentExtras(contentPlus);

            Drive drive = buildDrive();
            Drive.Files.Create rquest = drive.files().create(content, fileContent);
            // default: resume, If the file Size is less than 20M, use directly upload.
            boolean isDirectUpload = false;
            if (io.length() < DIRECT_UPLOAD_MAX_SIZE) {
                isDirectUpload = true;
            }
            rquest.getMediaHttpUploader().setDirectUploadEnabled(isDirectUpload);
            mFile = rquest.execute();

            Logger.i(TAG, "executeFilesCreateFile:" + mFile.toString());
            sendHandleMessage(R.id.drive_files_button_createfile, SUCCESS);
        } catch (Exception e) {
            sendHandleMessage(R.id.drive_files_button_createfile, FAIL);
            Logger.e(TAG, "executeFilesCreateFile exception: " + e.toString());
        }
    }

    /**
     * Generate and obtain the base64 code of the thumbnail.
     *
     * @return base64 code of the thumbnail
     */
    public byte[] getThumbnailImage(String iamgeFileName) {
        //imagePath: path to store thumbnail image
        String imagePath = "/storage/emulated/0/DCIM/Camera/";
        ThumbnailUtilsImage.genImageThumbnail(iamgeFileName, imagePath + "imageThumbnail.jpg", 250, 150, 0);
        try (FileInputStream is = new FileInputStream(imagePath + "imageThumbnail.jpg")) {
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            return buffer;
        } catch (IOException ex) {
            Logger.e(TAG, ex.getMessage());
            return null;
        }
    }

    /**
     * Execute the Files.get interface test task
     */
    public void executeFilesGet() {
        TaskManager.getInstance().execute(new HmsServiceManager.FilesGetTask());
    }

    /**
     * The Files.get interface test task
     */
    public class FilesGetTask extends DriveTask {
        @Override
        public void call() {
            downLoadFile(mFile.getId());
        }
    }
    public void downLoadTXTFile(String fileId){

    }
    /**
     * Test Files.get interface
     *
     * @param fileId Specifies the file to be obtained.
     */
    public void downLoadFile(String fileId) {
        try {
            if (fileId == null) {
                Logger.e(TAG, "executeFilesGet error, need to create file.");
                sendHandleMessage(R.id.drive_files_button_get, FAIL);
                return;
            }
            String imagePath = "/storage/emulated/0/DCIM/Camera/";
            Drive drive = buildDrive();
            // Get File metaData
            Drive.Files.Get request = drive.files().get(fileId);
            request.setFields("id,size");
            File res = request.execute();
            // Download File
            long size = res.getSize();
            Drive.Files.Get get = drive.files().get(fileId);
            get.setForm("media");
            MediaHttpDownloader downloader = get.getMediaHttpDownloader();

            boolean isDirectDownload = false;
            if (size < DIRECT_DOWNLOAD_MAX_SIZE) {
                isDirectDownload = true;
            }
            downloader.setContentRange(0, size - 1).setDirectDownloadEnabled(isDirectDownload);
            downloader.setProgressListener(new MediaHttpDownloaderProgressListener() {
                @Override
                public void progressChanged(MediaHttpDownloader mediaHttpDownloader) throws IOException {
                    // The download subthread invokes this method to process the download progress.
                    double progress = mediaHttpDownloader.getProgress();
                }
            });
            java.io.File f = new java.io.File(imagePath + "download.jpg");
            get.executeContentAndDownloadTo(new FileOutputStream(f));

            Logger.i(TAG, "executeFilesGetMedia success.");
            sendHandleMessage(R.id.drive_files_button_get, SUCCESS);
        } catch (Exception e) {
            sendHandleMessage(R.id.drive_files_button_get, FAIL);
            Logger.e(TAG, "executeFilesGet exception: " + e.toString());
        }
    }

    /**
     * Execute the Files.copy interface test task
     */
    public void executeFilesCopy() {
        TaskManager.getInstance().execute(new HmsServiceManager.FilesCopyTask());
    }

    /**
     * The Files.copy interface test task
     */
    public class FilesCopyTask extends DriveTask {

        @Override
        public void call() {
            try {
                Drive drive = buildDrive();
                Drive.Files.List fileListReq = drive.files().list();
                fileListReq.setQueryParam("mimeType = 'application/vnd.huawei-apps.folder'").setOrderBy("name").setPageSize(100).setFields("*");
                FileList fileList = fileListReq.execute();
                ArrayList<String> dstDir = getParentsId(fileList);
                Logger.e(TAG, "copyFile Source File Sharded Status: " + mFile.getHasShared());
                copyFile(mFile, dstDir);
            } catch (IOException e) {
                Logger.e(TAG, "copyFile -- list file error: " + e.toString());
                sendHandleMessage(R.id.drive_files_button_copy, FAIL);
            }
        }
    }

    /**
     * copy file
     *
     * @param file copy file
     * @param dstDir Specifies the destination directory of the file to be copied.
     */
    public void copyFile(File file, ArrayList<String> dstDir) {
        try {

            // Copy operation, copy to the first created directory
            File copyFile = new File();
            if (file == null || file.getFileName() == null || dstDir == null) {
                Logger.e(TAG, "copyFile arguments error");
                sendHandleMessage(R.id.drive_files_button_copy, FAIL);
                return;
            }

            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String suffix = formatter.format(new Date());
            copyFile.setFileName(file.getFileName() + "_copy" + "_" + suffix);
            copyFile.setDescription("copyFile");
            copyFile.setParentFolder(dstDir);
            copyFile.setFavorite(true);
            copyFile.setEditedTime(new DateTime(System.currentTimeMillis()));

            Drive drive = buildDrive();
            Drive.Files.Copy copyFileReq = drive.files().copy(file.getId(), copyFile);
            copyFileReq.setFields("*");
            File result = copyFileReq.execute();
            Logger.i(TAG, "copyFile: " + result.toString());
            sendHandleMessage(R.id.drive_files_button_copy, SUCCESS);
        } catch (IOException ex) {
            Logger.e(TAG, "copyFile error: " + ex.toString());
            sendHandleMessage(R.id.drive_files_button_copy, FAIL);
        }
    }

    /**
     * The Files.delete interface test task, use to delete file or directory
     */
    public class FilesDeleteTask extends DriveTask {
        @Override
        public void call() {
            //Create a folder and delete it
            File dir = getDirectory();
            deleteFile(dir.getId());
        }
    }

    /**
     * Create a directory to test deleteFile
     *
     * @return file
     */
    public File getDirectory() {
        File uploadFile = null;
        // Newly created directory
        Drive drive = buildDrive();
        Map<String, String> appProperties = new HashMap<>();
        appProperties.put("test", "property");
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS");
        String dir = formatter.format(new Date());
        File file = new File();
        file.setFileName(dir).setAppSettings(appProperties).setMimeType("application/vnd.huawei-apps.folder");
        try {
            uploadFile = drive.files().create(file).execute();
        } catch (IOException e) {
            Logger.e(TAG, e.toString());
        }
        return uploadFile;
    }

    /**
     * Delete files (directories) from the recycle bin
     *
     * @param fileId file ID
     */
    public void deleteFile(String fileId) {
        if (fileId == null) {
            Logger.i(TAG, "deleteFile error, need to create file");
            sendHandleMessage(R.id.drive_files_button_delete, FAIL);
        }
        try {
            Drive drive = buildDrive();
            Drive.Files.Delete deleteFileReq = drive.files().delete(fileId);
            deleteFileReq.execute();
            Logger.i(TAG, "deleteFile result: " + deleteFileReq.toString());
            sendHandleMessage(R.id.drive_files_button_delete, SUCCESS);
        } catch (IOException ex) {
            sendHandleMessage(R.id.drive_files_button_delete, FAIL);
            Logger.e(TAG, "deleteFile error: " + ex.toString());
        }
    }

    /**
     * Execute the Files.update test task
     */
    public void executeFilesUpdateContent() {
        TaskManager.getInstance().execute(new HmsServiceManager.FilesUpdateContentTask());
    }

    /**
     * The Files.update interface test task
     */
    public class FilesUpdateContentTask extends DriveTask {
        @Override
        public void call() {
            String newFilePath = context.getExternalCacheDir().getAbsolutePath() + "/cache.jpg";
            uodateFile(mFile, newFilePath);
        }
    }

    /**
     * Update the metadata and content of the file.
     *
     * @param oldFile Specifies the old file to be updated.
     * @param newFilePath new File
     */
    void uodateFile(File oldFile, String newFilePath) {
        try {
            if (oldFile == null || TextUtils.isEmpty(newFilePath)) {
                Logger.e(TAG, "updateFileContent error, need to create file.");
                sendHandleMessage(R.id.drive_files_update_content_button, FAIL);
                return;
            }

            Drive drive = buildDrive();
            File content = new File();

            content.setFileName(oldFile.getFileName() + "_update").setMimeType(mimeType(".jpg")).setDescription("update image").setFavorite(true);

            java.io.File io = new java.io.File(newFilePath);
            FileContent fileContent = new FileContent(mimeType(io), io);
            Drive.Files.Update request = drive.files().update(oldFile.getId(), content, fileContent);
            boolean isDirectUpload = false;
            if (io.length() < DIRECT_UPLOAD_MAX_SIZE) {
                isDirectUpload = true;
            }

            request.getMediaHttpUploader().setDirectUploadEnabled(isDirectUpload);
            mFile = request.execute();

            Logger.i(TAG, "updateFileContent result: " + mFile.toString());
            sendHandleMessage(R.id.drive_files_update_content_button, SUCCESS);
        } catch (Exception e) {
            sendHandleMessage(R.id.drive_files_update_content_button, FAIL);
            Logger.e(TAG, "updateFile error: " + e.toString());
        }
    }

    /**
     * Execute the Files.delete interface test task
     */
    public void executeFilesDelete() {
        TaskManager.getInstance().execute(new HmsServiceManager.FilesDeleteTask());
    }

    /**
     * Execute the Files.emptyRecycle interface test task
     */
    public void executeFilesEmptyRecycle() {
        TaskManager.getInstance().execute(new HmsServiceManager.FilesEmptyRecycleTask());
    }

    /**
     * Execute the Files.emptyRecycle interface test task
     */
    public class FilesEmptyRecycleTask extends DriveTask {
        @Override
        public void call() {
            doFilesEmptyRecycle();
        }
    }

    /**
     * empty recycle bin
     */
    public void doFilesEmptyRecycle() {
        Drive drive = buildDrive();
        try {
            //create a new folder
            Map<String, String> appProperties = new HashMap<>();
            appProperties.put("property", "user_defined");
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS");
            String dir = formatter.format(new Date());
            File file = new File();
            file.setFileName(dir).setAppSettings(appProperties).setMimeType("application/vnd.huawei-apps.folder");
            File uploadFile = drive.files().create(file).execute();

            // Call update to the recycle bin
            File trashFile = new File();
            trashFile.setRecycled(true);
            drive.files().update(uploadFile.getId(), trashFile).execute();
            // Empty the recycle bin
            Drive.Files.EmptyRecycle response = drive.files().emptyRecycle();
            response.execute();
            String value = response.toString();
            Logger.i(TAG, "executeFilesEmptyRecycle" + value);
            sendHandleMessage(R.id.drive_files_button_emptyRecycle, SUCCESS);
        } catch (IOException e) {
            Logger.e(TAG, "executeFilesEmptyRecycle error: " + e.toString());
            sendHandleMessage(R.id.drive_files_button_emptyRecycle, FAIL);
        }
    }

    /**
     * Execute the Files.subscribe interface test task
     */
    public void executeFilesSubscribe() {
        TaskManager.getInstance().execute(new HmsServiceManager.FilesSubscribeTask());
    }

    /**
     * The Files.subscribe interface test task
     */
    public class FilesSubscribeTask extends DriveTask {

        @Override
        public void call() {
            filesWatch(mFile.getId());
        }
    }

    /**
     * watching for changes to a file
     *
     * @param fileId file ID
     */
    public void filesWatch(String fileId) {
        try {
            Drive drive = buildDrive();
            Channel content = new Channel();
            content.setId("id" + System.currentTimeMillis());
            content.setType("web_hook");
            content.setUrl("https://www.huawei.com/path/to/webhook");
            Drive.Files.Subscribe request = drive.files().subscribe(fileId, content);
            Channel channel = request.execute();
            //Object channel is used in other places.
            Logger.i(TAG, "channel: " + channel.toPrettyString());
            sendHandleMessage(R.id.drive_files_subscribe_button, SUCCESS);
        } catch (IOException e) {
            sendHandleMessage(R.id.drive_files_subscribe_button, FAIL);
            Logger.e(TAG, "executeFilesSubscribe error: " + e.toString());
        }
    }

    /**
     * Execute the Changes.startCursor interface test task.
     */
    public void executeChangesGetStartCursor() {
        TaskManager.getInstance().execute(new HmsServiceManager.ChangesGetStartCursorTask());
    }

    /**
     * The Changes.startCursor interface test task.
     */
    public class ChangesGetStartCursorTask extends DriveTask {
        @Override
        public void call() {
            doGetStartCursor();
        }
    }

    /**
     * In the future, the file will be changed. This gets the starting cursor of the changes
     */
    public void doGetStartCursor() {
        try {
            Drive drive = buildDrive();
            Drive.Changes.GetStartCursor request = drive.changes().getStartCursor();
            request.setFields("*");
            StartCursor startPageToken = request.execute();
            Logger.i(TAG, "GetStartCursor: " + startPageToken.toString());
            sendHandleMessage(R.id.drive_changes_getstartcursor_button, SUCCESS);
        } catch (Exception e) {
            sendHandleMessage(R.id.drive_changes_getstartcursor_button, FAIL);
            Logger.e(TAG, "executeChangesGetStartCursor error: " + e.toString());
        }
    }
}
