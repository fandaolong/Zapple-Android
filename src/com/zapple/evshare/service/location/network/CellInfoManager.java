/*
 * Copyright (C) 2012 Zapple, www.zapple.com.cn
 */

package com.zapple.evshare.service.location.network;

import android.content.Context;
import android.telephony.CellLocation;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CellInfoManager {
    private static final String TAG = CellInfoManager.class.getSimpleName();
    private static final boolean DEBUG = true;
    private boolean isCdma = false;
    private boolean isGsm = false;
    private int asu;// Signal strength
    private int bid; // cdma, base station id - cellid
    private int nid; // cdma, cdma network identification number - lac
    private int sid; // cdma, cdma system identification number - mnc
    private int cid; // gsm, cellid
    private int lac; // gsm, gsm location code
    private int mcc;
    private int mnc;
    private final PhoneStateListener mPhoneStateListener;
    private TelephonyManager mTelephonyManager;
    private boolean mValid;

    public CellInfoManager(Context context) {
        mPhoneStateListener = new CellInfoListener(this);
        mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CELL_LOCATION | PhoneStateListener.LISTEN_SIGNAL_STRENGTH);
    }

    public void quit() {
        mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
    }

    public void dumpMe() {
        StringBuilder dump = new StringBuilder();
        if (isCdma) {
            dump.append("phone type is cdma");
            dump.append("\n signal strength, asu: " + asu);
            dump.append("\n base station id, bid: " + bid);
            dump.append("\n network identity number, nid: " + nid);
            dump.append("\n system identification number, sid: " + sid);
        } else {
            dump.append("phone type is gsm");
            dump.append("\n cell id: " + cid);
            dump.append("\n lac: " + lac);
        }

        dump.append("\n mcc: " + mcc);
        dump.append("\n mnc: " + mnc);

        if (DEBUG) Log.d(TAG, "CellInfo dump:" + dump.toString());
    }

    private int dBm(int rssi) {
        int j;
        if (rssi >= 0 && rssi <= 31) {
            j = rssi * 2 + -113;
        } else {
            j = 0;
        }
        return j;
    }

    private int asu() {
        return asu;
    }

    public int bid() {
        if (!mValid) {
            update();
        }
        return bid;
    }

    public JSONArray cellTowers() {
        JSONArray jsonarray = new JSONArray();
        // dumpMe();
        List<Cell> cells = dumpCells();
        if (isGsm()) {
            for (Cell cell : cells) {
                try {
                    JSONObject jsonobject = new JSONObject();
                    jsonobject.put("cell_id", cell.cid);
                    jsonobject.put("location_area_code", lac());
                    jsonobject.put("mobile_country_code", mcc());
                    jsonobject.put("mobile_network_code", mnc());
                    jsonobject.put("signal_strength", dBm(cell.asu));
                    jsonobject.put("age", 0);
                    jsonarray.put(jsonobject);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    if (DEBUG) Log.e(TAG, "failed in collect gsm tower",ex);
                }
            }
        } else {
            for (Cell cell : cells) {
                try {
                    JSONObject jsonobject = new JSONObject();
                    jsonobject.put("cell_id", cell.cid);
                    jsonobject.put("location_area_code", nid());
                    jsonobject.put("mobile_country_code", mcc());
                    jsonobject.put("mobile_network_code", sid());
                    jsonobject.put("signal_strength", dBm(cell.asu));
                    jsonobject.put("age", 0);
                    jsonarray.put(jsonobject);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    if (DEBUG) Log.e(TAG, "failed in collect cdma towers", ex);
                }
            }
        }
        return jsonarray;
    }

    private int cid() {
        if (!mValid) {
            update();
        }
        return cid;
    }

    /**
     * @return
     */
    public List<Cell> dumpCells() {
        ArrayList<Cell> cells = new ArrayList<Cell>();
        if (isGsm()) {
            if (cid() == 0) {
                return cells;
            }
            cells.add(new Cell(cid(), asu()));
        } else {
            if (bid() == 0) {
                return cells;
            }
            cells.add(new Cell(bid(), asu()));
        }

        List<NeighboringCellInfo> neighborCellInfo = mTelephonyManager.getNeighboringCellInfo();
        if (neighborCellInfo == null || neighborCellInfo.size() == 0) {
            if (DEBUG) Log.e(TAG, "----------------------------------------->no neighbor station");
            return cells;
        }
        for (NeighboringCellInfo info : neighborCellInfo) {
            int cid = info.getCid();
            if ((cid <= 0) || (cid == 65535)) {
                continue;
            }
            cells.add(new Cell(cid, info.getRssi()));
        }
        return cells;
    }

    public boolean isCdma() {
        if (!mValid) {
            update();
        }
        return isCdma;
    }

    public boolean isGsm() {
        if (!mValid) {
            update();
        }
        return isGsm;
    }

    private int lac() {
        if (!mValid) {
            update();
        }
        return lac;
    }

    private int mcc() {
        if (!mValid) {
            update();
        }
        return mcc;
    }

    private int mnc() {
        if (!mValid) {
            update();
        }
        return mnc;
    }

    private int nid() {
        if (!mValid) {
            update();
        }
        return nid;
    }

    public float score() {
        float score = 0f;
        if (isCdma()) {
            return 1065353216;
        }
        if (isGsm()) {
            score = 0.0F;
            List<Cell> cells = dumpCells();
            if (cells.size() > 0) {
                for (Cell cell : cells) {
                    int asu = cell.asu;
                    if (asu < 0 || asu > 31) {
                        score += 0.5F;
                    } else {
                        score += 1.0F;
                    }
                }
            } else {
                score = 1065353216;
            }
        }
        return score;
    }

    private int sid() {
        if (!mValid) {
            update();
        }
        return sid;
    }

    private synchronized void update() {
        isGsm = false;
        isCdma = false;
        cid = 0;
        lac = 0;
        mcc = 0;
        mnc = 0;
        bid = 0; // cid for cdma
        nid = 0; // lac for cdma
        sid = 0; // mnc for cdma

        CellLocation cellLocation = mTelephonyManager.getCellLocation();
        int nPhoneType = mTelephonyManager.getPhoneType();
        switch (nPhoneType) {
            case TelephonyManager.PHONE_TYPE_GSM: {
                try {
                    String strNetworkOperator = mTelephonyManager.getNetworkOperator();
                    int nNetworkOperatorLength = strNetworkOperator.length();
                    mcc = Integer.parseInt(strNetworkOperator.substring(0, 3));
                    mnc = Integer.parseInt(strNetworkOperator.substring(3, nNetworkOperatorLength));
                } catch (Exception e) {
                    if (DEBUG) Log.e(TAG, "", e);
                }
                if (cellLocation instanceof GsmCellLocation) {
                    isGsm = true;
                    GsmCellLocation gsmLoc = (GsmCellLocation) cellLocation;
                    int gsmCid = gsmLoc.getCid();
                    if (gsmCid > 0) {
                        if (gsmCid != 65535) {
                            cid = gsmCid;
                            lac = gsmLoc.getLac();
                        }
                    }
                    mValid = true;
                }
                break;
            }
            case TelephonyManager.PHONE_TYPE_CDMA: {
                if (cellLocation instanceof CdmaCellLocation) {
                    CdmaCellLocation cdmaLoc = (CdmaCellLocation) cellLocation;
                    bid = cdmaLoc.getBaseStationId();
                    nid = cdmaLoc.getNetworkId();
                    sid = cdmaLoc.getSystemId();
                    try {
                        //mcc = Integer.valueOf(mTelephonyManager.getNetworkOperator().substring(0, 3));
                        mnc = sid;
                    } catch (Exception e) {
                        if (DEBUG) Log.e(TAG, "", e);
                    }
                    isCdma = true;
                    mValid = true;
                }
                break;
            }
            case TelephonyManager.PHONE_TYPE_NONE:
            default:
                break;
        }
    }

    class CellInfoListener extends PhoneStateListener {
        CellInfoListener(CellInfoManager manager) {
        }

        public void onCellLocationChanged(CellLocation paramCellLocation) {
            mValid = false;
        }

        public void onSignalStrengthChanged(int paramInt) {
            asu = paramInt;
        }
    }

    public class Cell implements Comparable<Cell> {
        public int cid;
        public int asu;

        public Cell(int cid, int asu) {
            this.cid = cid;
            this.asu = asu;
        }

        @Override
        public int compareTo(Cell another) {
            return this.cid - another.cid;
        }
    }
}
