package com.huawei.cloud.drive.hms;

import android.content.Context;
import android.util.Log;

import com.huawei.agconnect.cloud.database.AGConnectCloudDB;
import com.huawei.agconnect.cloud.database.CloudDBZone;
import com.huawei.agconnect.cloud.database.CloudDBZoneConfig;
import com.huawei.agconnect.cloud.database.CloudDBZoneObjectList;
import com.huawei.agconnect.cloud.database.CloudDBZoneQuery;
import com.huawei.agconnect.cloud.database.CloudDBZoneSnapshot;
import com.huawei.agconnect.cloud.database.ListenerHandler;
import com.huawei.agconnect.cloud.database.OnSnapshotListener;
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException;
import com.huawei.cloud.drive.bean.MemoInfo;
import com.huawei.cloud.drive.bean.ObjectTypeInfoHelper;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Proxying implementation of CloudDBZone.
 */
public class CloudDBManager {
    private static final String TAG = "CloudDBManager";

    private AGConnectCloudDB mCloudDB;

    private CloudDBZone mCloudDBZone;

    private ListenerHandler mRegister;

    private CloudDBZoneConfig mConfig;

    private UiCallBack mUiCallBack = UiCallBack.DEFAULT;
    private CloudDBZoneOpenCallback mCloudDBZoneOpenCallback;
    public interface CloudDBZoneOpenCallback {
        void onCloudDBZoneOpened(CloudDBZone cloudDBZone);
    }
    public interface MemoCallback {
        void onSuccess(List<MemoInfo> memoInfoList);
        void onFailure(String errorMessage);
    }
    private int mMemoIndex = 0;

    private ReadWriteLock mReadWriteLock = new ReentrantReadWriteLock();

    /**
     * Monitor data change from database. Update memo info list if data have changed
     */
    private OnSnapshotListener<MemoInfo> mSnapshotListener = new OnSnapshotListener<MemoInfo>() {
        @Override
        public void onSnapshot(CloudDBZoneSnapshot<MemoInfo> cloudDBZoneSnapshot, AGConnectCloudDBException e) {
            if (e != null) {
                Log.w(TAG, "onSnapshot: " + e.getMessage());
                return;
            }
            CloudDBZoneObjectList<MemoInfo> snapshotObjects = cloudDBZoneSnapshot.getSnapshotObjects();
            List<MemoInfo> memoInfoList = new ArrayList<>();
            try {
                if (snapshotObjects != null) {
                    while (snapshotObjects.hasNext()) {
                        MemoInfo memoInfo = snapshotObjects.next();
                        memoInfoList.add(memoInfo);
                        updateMemoIndex(memoInfo);
                    }
                }
                mUiCallBack.onSubscribe(memoInfoList);
            } catch (AGConnectCloudDBException snapshotException) {
                Log.w(TAG, "onSnapshot:(getObject) " + snapshotException.getMessage());
            } finally {
                cloudDBZoneSnapshot.release();
            }
        }
    };

    public CloudDBManager() {
        mCloudDB = AGConnectCloudDB.getInstance();
    }

    /**
     * Init AGConnectCloudDB in Application
     *
     * @param context application context
     */
    public static void initAGConnectCloudDB(Context context) {
        AGConnectCloudDB.initialize(context);
    }

    /**
     * Call AGConnectCloudDB.createObjectType to init schema
     */
    public void createObjectType() {
        try {
            mCloudDB.createObjectType(ObjectTypeInfoHelper.getObjectTypeInfo());
        } catch (AGConnectCloudDBException e) {
            Log.w(TAG, "createObjectType: " + e.getMessage());
        }
    }

    /**
     * Call AGConnectCloudDB.openCloudDBZone to open a cloudDBZone.
     * We set it with cloud cache mode, and data can be store in local storage
     */
    public void openCloudDBZone() {
        mConfig = new CloudDBZoneConfig("quickStartMemoDemo",
                CloudDBZoneConfig.CloudDBZoneSyncProperty.CLOUDDBZONE_CLOUD_CACHE,
                CloudDBZoneConfig.CloudDBZoneAccessProperty.CLOUDDBZONE_PUBLIC);
        mConfig.setPersistenceEnabled(true);
        try {
            mCloudDBZone = mCloudDB.openCloudDBZone(mConfig, true);
        } catch (AGConnectCloudDBException e) {
            Log.w(TAG, "openCloudDBZone: " + e.getMessage());
        }
    }

    public void openCloudDBZoneV2(CloudDBZoneOpenCallback callback) {
        mCloudDBZoneOpenCallback = callback;
        mConfig = new CloudDBZoneConfig("quickStartMemoDemo",
                CloudDBZoneConfig.CloudDBZoneSyncProperty.CLOUDDBZONE_CLOUD_CACHE,
                CloudDBZoneConfig.CloudDBZoneAccessProperty.CLOUDDBZONE_PUBLIC);
        mConfig.setPersistenceEnabled(true);
        Task<CloudDBZone> openDBZoneTask = mCloudDB.openCloudDBZone2(mConfig, true);
        openDBZoneTask.addOnSuccessListener(new OnSuccessListener<CloudDBZone>() {
            @Override
            public void onSuccess(CloudDBZone cloudDBZone) {
                Log.i(TAG, "Open cloudDBZone success");
                mCloudDBZone = cloudDBZone;
                if (mCloudDBZoneOpenCallback != null) {
                    mCloudDBZoneOpenCallback.onCloudDBZoneOpened(cloudDBZone);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.w(TAG, "Open cloudDBZone failed for " + e.getMessage());
            }
        });
    }

    /**
     * Call AGConnectCloudDB.closeCloudDBZone
     */
    public void closeCloudDBZone() {
        try {
            mRegister.remove();
            mCloudDB.closeCloudDBZone(mCloudDBZone);
        } catch (AGConnectCloudDBException e) {
            Log.w(TAG, "closeCloudDBZone: " + e.getMessage());
        }
    }

    /**
     * Call AGConnectCloudDB.deleteCloudDBZone
     */
    public void deleteCloudDBZone() {
        try {
            mCloudDB.deleteCloudDBZone(mConfig.getCloudDBZoneName());
        } catch (AGConnectCloudDBException e) {
            Log.w(TAG, "deleteCloudDBZone: " + e.getMessage());
        }
    }

    /**
     * Add a callback to update memo info list
     *
     * @param uiCallBack callback to update memo list
     */
    public void addCallBacks(UiCallBack uiCallBack) {
        mUiCallBack = uiCallBack;
    }

    /**
     * Add mSnapshotListener to monitor data changes from storage
     */
    /**
     * Query all memos in storage from cloud side with CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY
     */
    public void queryAllMemos() {
        if (mCloudDBZone == null) {
            Log.w(TAG, "CloudDBZone is null, try re-open it");
            return;
        }
        Task<CloudDBZoneSnapshot<MemoInfo>> queryTask = mCloudDBZone.executeQuery(
                CloudDBZoneQuery.where(MemoInfo.class),
                CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY);
        queryTask.addOnSuccessListener(new OnSuccessListener<CloudDBZoneSnapshot<MemoInfo>>() {
            @Override
            public void onSuccess(CloudDBZoneSnapshot<MemoInfo> snapshot) {
                processQueryResult(snapshot);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                mUiCallBack.updateUiOnError("Query memo list from cloud failed");
            }
        });
    }

    /**
     * Query memos with condition
     *
     * @param query query condition
     */
    public void queryMemos(CloudDBZoneQuery<MemoInfo> query) {
        if (mCloudDBZone == null) {
            Log.w(TAG, "CloudDBZone is null, try re-open it");
            return;
        }

        Task<CloudDBZoneSnapshot<MemoInfo>> queryTask = mCloudDBZone.executeQuery(query,
                CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY);
        queryTask.addOnSuccessListener(new OnSuccessListener<CloudDBZoneSnapshot<MemoInfo>>() {
            @Override
            public void onSuccess(CloudDBZoneSnapshot<MemoInfo> snapshot) {
                processQueryResult(snapshot);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                mUiCallBack.updateUiOnError("Query failed");
            }
        });
    }

    private void processQueryResult(CloudDBZoneSnapshot<MemoInfo> snapshot) {
        CloudDBZoneObjectList<MemoInfo> memoInfoCursor = snapshot.getSnapshotObjects();
        List<MemoInfo> memoInfoList = new ArrayList<>();
        try {
            while (memoInfoCursor.hasNext()) {
                MemoInfo memoInfo = memoInfoCursor.next();
                memoInfoList.add(memoInfo);
            }
        } catch (AGConnectCloudDBException e) {
            Log.w(TAG, "processQueryResult: " + e.getMessage());
        } finally {
            snapshot.release();
        }
        mUiCallBack.onAddOrQuery(memoInfoList);
    }


    public void upsertMemoInfos(MemoInfo memoInfo) {
        if (mCloudDBZone == null) {
            Log.w(TAG, "CloudDBZone is null, try re-open it");
            return;
        }

        Task<Integer> upsertTask = mCloudDBZone.executeUpsert(memoInfo);
        upsertTask.addOnSuccessListener(new OnSuccessListener<Integer>() {
            @Override
            public void onSuccess(Integer cloudDBZoneResult) {
                Log.i(TAG, "Upsert " + cloudDBZoneResult + " records");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                mUiCallBack.updateUiOnError("Insert memo info failed");
            }
        });
    }

    public void deleteMemoInfos(List<MemoInfo> memoInfoList) {
        if (mCloudDBZone == null) {
            Log.w(TAG, "CloudDBZone is null, try re-open it");
            return;
        }

        Task<Integer> deleteTask = mCloudDBZone.executeDelete(memoInfoList);
        if (deleteTask.getException() != null) {
            mUiCallBack.updateUiOnError("Delete memo info failed");
            return;
        }
        mUiCallBack.onDelete(memoInfoList);
    }

    private void updateMemoIndex(MemoInfo memoInfo) {
        try {
            mReadWriteLock.writeLock().lock();
            if (mMemoIndex < memoInfo.getId()) {
                mMemoIndex = Math.toIntExact(memoInfo.getId());
            }
        } finally {
            mReadWriteLock.writeLock().unlock();
        }
    }

    /**
     * Get max id of memoinfos
     *
     * @return max memo info id
     */
    public int getMemoIndex() {
        try {
            mReadWriteLock.readLock().lock();
            return mMemoIndex;
        } finally {
            mReadWriteLock.readLock().unlock();
        }
    }

    public interface UiCallBack {
        UiCallBack DEFAULT = new UiCallBack() {
            @Override
            public void onAddOrQuery(List<MemoInfo> memoInfoList) {
                Log.i(TAG, "Using default onAddOrQuery");
            }

            @Override
            public void onSubscribe(List<MemoInfo> memoInfoList) {
                Log.i(TAG, "Using default onSubscribe");
            }

            @Override
            public void onDelete(List<MemoInfo> memoInfoList) {
                Log.i(TAG, "Using default onDelete");
            }

            @Override
            public void updateUiOnError(String errorMessage) {
                Log.i(TAG, "Using default updateUiOnError");
            }
        };

        void onAddOrQuery(List<MemoInfo> memoInfoList);

        void onSubscribe(List<MemoInfo> memoInfoList);

        void onDelete(List<MemoInfo> memoInfoList);

        void updateUiOnError(String errorMessage);
    }
}