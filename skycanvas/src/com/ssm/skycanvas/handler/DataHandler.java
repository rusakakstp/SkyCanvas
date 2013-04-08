package com.ssm.skycanvas.handler;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

import org.apache.cassandra.thrift.Cassandra;
import org.apache.cassandra.thrift.Cassandra.Client;
import org.apache.cassandra.thrift.Column;
import org.apache.cassandra.thrift.ColumnParent;
import org.apache.cassandra.thrift.Compression;
import org.apache.cassandra.thrift.ConsistencyLevel;
import org.apache.cassandra.thrift.CqlResult;
import org.apache.cassandra.thrift.CqlRow;
import org.apache.cassandra.thrift.InvalidRequestException;
import org.apache.cassandra.thrift.TimedOutException;
import org.apache.cassandra.thrift.UnavailableException;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.apache.cassandra.utils.UUIDGen;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransportException;

import sun.util.logging.resources.logging;

import android.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;

import com.ssm.skycanvas.data.MarkerData;
import com.ssm.skycanvas.view.AugmentedImageView;
import com.ssm.skycanvas.view.CommonUtil;

public class DataHandler {
	public static String CLUSTER_NAME = "Test Cluster";
	public static String KEY_SPACE_NAME = "skycanvas";
	public static String COLUMN_FAMILY_NAME = "markers";
	public static String HOST = "121.177.66.251";
	public static int PORT = 9160;
	private CqlResult cql;

	private static final String UTF8 = "UTF8";
	private final String ROWKEY = "key";
	private final String USERID = "userid";
	private final String ACTIVATED = "activated";
	private final String LATITUDE = "latitude";
	private final String LONGTITUDE = "longtitude";
	private final String ALTITUDE = "altitude";
	private final String AREA = "area";
	private final String TITLE = "title";
	private final String MEMO = "memo";
	private final String PICDATA = "picdata";

	private Context context;
	private AugmentedViewHandler augViewHandler;
	private Client client;
	private TSocket transport;
	private TFramedTransport framedTransport;
	private TBinaryProtocol protocol;
	private ColumnParent parent;

	private ArrayList<MarkerData> markers;

	private ArrayList<MarkerData> tempMarkers;
	private int flag_add = 0;

	private double scanRadius = 0;

	public AugmentedViewHandler getAugViewHandler() {
		return augViewHandler;
	}

	public void setAugViewHandler(AugmentedViewHandler augViewHandler) {
		this.augViewHandler = augViewHandler;
	}

	public double getScanRadius() {
		return scanRadius;
	}

	public void setScanRadius(double scanRadius) {
		this.scanRadius = scanRadius;
	}

	public DataHandler(Context context) {
		// TODO Auto-generated constructor stub
		this.context = context;
		augViewHandler = new AugmentedViewHandler(context);
		markers = new ArrayList<MarkerData>();

		transport = new TSocket(HOST, PORT);
		framedTransport = new TFramedTransport(transport);
		protocol = new TBinaryProtocol(framedTransport);
		client = new Cassandra.Client(protocol);
		parent = new ColumnParent("markers");

		tempMarkers = new ArrayList<MarkerData>();

		openStream();

		setScanRadius(100000);

	}

	public void openStream() {
		try {
			framedTransport.open();
			client.set_keyspace(KEY_SPACE_NAME);
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static byte[] asByteArray(java.util.UUID uuid) {
		long msb = uuid.getMostSignificantBits();
		long lsb = uuid.getLeastSignificantBits();
		byte[] buffer = new byte[16];

		for (int i = 0; i < 8; i++) {
			buffer[i] = (byte) (msb >>> 8 * (7 - i));
		}
		for (int i = 8; i < 16; i++) {
			buffer[i] = (byte) (lsb >>> 8 * (7 - i));
		}

		return buffer;
	}

	public void updateMarker(double latitude, double longtitude) {

		new UpdateMarkerAsync().execute(new Double[] { latitude, longtitude });

	}

	private void addNremoveMarkers(double x, double y) {

		int remove_flag = 0;
		int add_flag = 0;

		MarkerData marker;
		String query;

		double x_range = scanRadius * (Math.pow(LocationHandler.perLongtitude, -1));
		double y_range = scanRadius * (Math.pow(LocationHandler.perLatitude, -1));

		boolean flag_exist = false;
		String markerRowkey = null;
		String TempMarkerRowkey = null;
		int markerSize = markers.size();
		int tempMarkerSize = tempMarkers.size();

		StringBuffer queryRowKeySet = new StringBuffer();

		String colName = null;
		CommonUtil.tolog("markerSize  = " + markerSize);
		CommonUtil.tolog("tempMarkerSize  = " + tempMarkerSize);

		for (int i = 0; i < markers.size(); i++) {
			markerRowkey = markers.get(i).getRowKey();
//			CommonUtil.tolog("markerRowkey  = " + markerRowkey);
//			CommonUtil.tolog("i  = " + i);
			for (int j = 0; j < tempMarkers.size(); j++) {
				TempMarkerRowkey = tempMarkers.get(j).getRowKey();
//				CommonUtil.tolog("TempMarkerRowkey  = " + TempMarkerRowkey);
//				CommonUtil.tolog("j  = " + j);
				if (markerRowkey.equals(TempMarkerRowkey)) {
					flag_exist = true;
					tempMarkers.remove(j);
					break;
				}
			}

			if (flag_exist == false) {
				getAugViewHandler().removeView(markers.get(i));
				markers.remove(i);
				CommonUtil.tolog("remove View  = " + i);
				i--;
//				break;
			}
			
			flag_exist = false;

		}

		if (tempMarkers.size() != 0) {

			for (int i = 0; i < tempMarkers.size(); i++) {
				if (i == 0) {
					queryRowKeySet.append(tempMarkers.get(i).getRowKey());
				}
				else {
					queryRowKeySet.append(", " + tempMarkers.get(i).getRowKey());
				}
			}
			CommonUtil.tolog("queryRowKeySet.toString()  = " + queryRowKeySet.toString());

			tempMarkers.clear();

			query = "SELECT * FROM markers WHERE KEY IN (" + queryRowKeySet.toString() + ");";
			// query =
			// "SELECT * FROM markers WHERE key = '1259804d-3685-31b1-bd71-84f004aedc21'";
			// query = "SELECT * FROM markers ";

			queryRowKeySet.delete(0, queryRowKeySet.length());

			try {
				client.set_cql_version("3.0.0");
				cql = client.execute_cql_query(ByteBuffer.wrap(query.getBytes("UTF-8")), Compression.NONE);

				CommonUtil.tolog("------------insert complete---------------");
				CommonUtil.tolog("2st cql row size  = " + cql.getRowsSize());
				// CommonUtil.tolog("column size  = " +
				// cql.getRows().get(0).getColumnsSize());

				for (Iterator<CqlRow> iterator = cql.getRowsIterator(); iterator.hasNext();) {
					CqlRow row = (CqlRow) iterator.next();
					// CommonUtil.tolog("CqlRow  = ");
					marker = new MarkerData();
					for (Iterator<Column> iterator2 = row.getColumnsIterator(); iterator2.hasNext();) {
						Column col = (Column) iterator2.next();
						colName = new String(col.getName(), UTF8);
						if (colName.equals(USERID)) {
							marker.setUserid(new String(col.getValue(), UTF8));
							CommonUtil.tolog("marker.getUserid()  = " + marker.getUserid());
						}
						else if (colName.equals(ROWKEY)) {
							marker.setRowKey(UUIDGen.getUUID(ByteBuffer.wrap(col.getValue())).toString());
							CommonUtil.tolog("marker.getRowKey()  = " + marker.getRowKey());
						}
						else if (colName.equals(LATITUDE)) {
							marker.setLatitude(ByteBuffer.wrap(col.getValue()).getDouble());
							CommonUtil.tolog("marker.getLatitude()  = " + marker.getLatitude());
						}
						else if (colName.equals(LONGTITUDE)) {
							marker.setLongtitude(ByteBuffer.wrap(col.getValue()).getDouble());
							CommonUtil.tolog("marker.getLongtitude()  = " + marker.getLongtitude());
						}
						else if (colName.equals(ALTITUDE)) {
							marker.setAltitude(ByteBuffer.wrap(col.getValue()).getDouble());
							CommonUtil.tolog("marker.getAltitude()  = " + marker.getAltitude());
						}
						else if (colName.equals(AREA)) {
							marker.setArea(ByteBuffer.wrap(col.getValue()).getDouble());
							CommonUtil.tolog("marker.getArea()  = " + marker.getArea());
						}
						else if (colName.equals(TITLE)) {
							marker.setTitle(new String(col.getValue(), UTF8));
							CommonUtil.tolog("marker.getTitle()  = " + marker.getTitle());
						}
						else if (colName.equals(MEMO)) {
							marker.setMemo(new String(col.getValue(), UTF8));
							CommonUtil.tolog("marker.getMemo()  = " + marker.getMemo());
						}
						else if (colName.equals(PICDATA)) {

							byte[] picdata = col.getValue();
							Bitmap picBM = BitmapFactory.decodeByteArray(picdata, 0, picdata.length);
							marker.setPic(picBM);
						}
						else {
							// CommonUtil.tolog("marker.name  = " + colName);
							// CommonUtil.tolog("marker.else  = " +
							// UUIDGen.getUUID(ByteBuffer.wrap(col.getValue())).toString());
						}
					}

					tempMarkers.add(marker);

				}

				for (int i = 0; i < tempMarkers.size(); i++) {
					this.markers.add(tempMarkers.get(i));
					getAugViewHandler().addview(tempMarkers.get(i));
					CommonUtil.tolog("addview");
				}
				
				tempMarkers.clear();

				// add n remove markers
				// CommonUtil.tolog("tempMarkers size = " + tempMarkers.size());
			}
			catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
		else {
//			for (int i = 0; i < tempMarkers.size(); i++) {
//				this.markers.add(tempMarkers.get(i));
//				getAugViewHandler().addview(tempMarkers.get(i));
//			}
//			tempMarkers.clear();
		}

	}

	public class UpdateMarkerAsync extends AsyncTask<Double, Void, Void> {

		private Double temp_x;
		private Double temp_y;

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Double... params) {
			// TODO Auto-generated method stub

			int temp = 0;

			// double length = Math.sqrt( Math.pow(LocationHandler.LATITUDE -
			// latitude, 2)+Math.pow(zeroToimageLongtitude, 2));
			MarkerData marker;
			String query;

			double x_range = scanRadius * (Math.pow(LocationHandler.perLongtitude, -1));
			double y_range = scanRadius * (Math.pow(LocationHandler.perLatitude, -1));

			// CommonUtil.tolog("---------------------------");
			// CommonUtil.tolog("scanRadius = " + scanRadius);
			// CommonUtil.tolog("x_range = " + x_range);
			// CommonUtil.tolog("y_range = " + y_range);

			temp_x = params[1];
			temp_y = params[0];

			query = "SELECT key FROM markers WHERE activated = '1' AND longtitude >= " + (temp_x - x_range) + " AND longtitude <= " + (temp_x + x_range) + " AND latitude >= " + (temp_y - y_range) + " AND latitude <= " + (temp_y + y_range) + "";

			// CommonUtil.tolog("query = " + query);
			// CommonUtil.tolog("---------------------------");

			try {

				cql = client.execute_cql_query(ByteBuffer.wrap(query.getBytes("UTF-8")), Compression.NONE);
				CommonUtil.tolog("1st cql row size  = " + cql.getRowsSize());
				// CommonUtil.tolog("tempMarkers.size()  = " +
				// tempMarkers.size());

				if (cql.getRowsSize() != markers.size()) {
					flag_add = 1;
					tempMarkers = makeTempMarkerArray(cql);
					CommonUtil.tolog("1st make temp marker array");
				}
				else {
					flag_add = 0;
				}
			}
			catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if (flag_add == 1) {
				addNremoveMarkers(temp_x, temp_y);
			}
		}

	}

	private ArrayList<MarkerData> makeTempMarkerArray(CqlResult cql) throws UnsupportedEncodingException {

		ArrayList<MarkerData> temp = new ArrayList<MarkerData>();
		MarkerData tempMarker;
//		tempMarkers.clear();

		for (Iterator<CqlRow> iterator = cql.getRowsIterator(); iterator.hasNext();) {
			CqlRow row = (CqlRow) iterator.next();
			tempMarker = new MarkerData();
			for (Iterator<Column> iterator2 = row.getColumnsIterator(); iterator2.hasNext();) {
				Column col = (Column) iterator2.next();

				if (new String(col.getName(), UTF8).equals(ROWKEY)) {
					tempMarker.setRowKey(UUIDGen.getUUID(ByteBuffer.wrap(col.getValue())).toString());
					// CommonUtil.tolog("--------------------------row key check col.getValue()  = "
					// + col.getValue());
					// CommonUtil.tolog("--------------------------row key check UUIDGen.getUUID(ByteBuffer.wrap(col.getValue())).toString();  = "
					// +
					// UUIDGen.getUUID(ByteBuffer.wrap(col.getValue())).toString());
				}

			}

			temp.add(tempMarker);

		}

		 CommonUtil.tolog("1st tempmarkers size = "+temp.size());
		return temp;

	}

	private void printMaers() {
		CommonUtil.tolog("marker check=====================start");
		for (Iterator<MarkerData> iterator = this.markers.iterator(); iterator.hasNext();) {
			MarkerData marker = (MarkerData) iterator.next();
			CommonUtil.tolog("marker getRowKey = " + marker.getRowKey());
		}
		CommonUtil.tolog("marker check=====================end");
	}

	private void printrow(CqlResult cql) {

		int rowSize = cql.rows.size();
		int columnSize = cql.rows.get(0).getColumnsSize();

		for (int i = 0; i < rowSize; i++) {
			CommonUtil.tolog("row " + i);
			for (int j = 0; j < columnSize; j++) {
				try {
					CommonUtil.tolog(new String(cql.rows.get(i).getColumns().get(j).getName(), UTF8) + " : " + new String(cql.rows.get(i).getColumns().get(j).getValue(), UTF8));
				}
				catch (Exception e) {
					// TODO: handle exception
					// e.printStackTrace();
				}
				try {
					CommonUtil.tolog(new String(cql.rows.get(i).getColumns().get(j).getName(), UTF8) + " : " + new String(UUID.nameUUIDFromBytes(cql.rows.get(i).getColumns().get(j).getValue()).toString()));
				}
				catch (Exception e) {
					// TODO: handle exception
					// e.printStackTrace();
				}
				try {
					CommonUtil.tolog(new String(cql.rows.get(i).getColumns().get(j).getName(), UTF8) + " : " + ByteBuffer.wrap(cql.rows.get(i).getColumns().get(j).getValue()).getDouble());
				}
				catch (Exception e) {
					// TODO: handle exception
					// e.printStackTrace();
				}
				try {
					CommonUtil.tolog(new String(cql.rows.get(i).getColumns().get(j).getName(), UTF8) + " : " + ByteBuffer.wrap(cql.rows.get(i).getColumns().get(j).getValue()).getInt(1));
				}
				catch (Exception e) {
					// TODO: handle exception
					// e.printStackTrace();
				}
			}
		}
	}
}

// private void addNremoveMarkers(double x, double y) {
//
// int remove_flag = 0;
// int add_flag = 0;
//
// MarkerData marker;
// String query;
//
// double x_range = scanRadius * (Math.pow(LocationHandler.perLongtitude, -1));
// double y_range = scanRadius * (Math.pow(LocationHandler.perLatitude, -1));
//
// if (this.markers.size() == 0) {
// for (int i = 0; i < tempMarkers.size(); i++) {
// this.markers.add(tempMarkers.get(i));
// getAugViewHandler().addview(tempMarkers.get(i));
// }
// }
//
// flag_add = 1;
//
// query = "SELECT * FROM markers WHERE activated = '1' AND longtitude >= " + (x
// - x_range) + " AND longtitude <= " + (x + x_range) + " AND latitude >= " + (y
// - y_range) + " AND latitude <= " + (y + y_range) + "";
//
// try {
//
// cql = client.execute_cql_query(ByteBuffer.wrap(query.getBytes("UTF-8")),
// Compression.NONE);
//
// CommonUtil.tolog("------------insert complete---------------");
// CommonUtil.tolog("cql row size  = " + cql.getRowsSize());
//
// for (Iterator<CqlRow> iterator = cql.getRowsIterator(); iterator.hasNext();)
// {
// CqlRow row = (CqlRow) iterator.next();
// CommonUtil.tolog("CqlRow  = ");
// marker = new MarkerData();
// for (Iterator<Column> iterator2 = row.getColumnsIterator();
// iterator2.hasNext();) {
// Column col = (Column) iterator2.next();
// if (new String(col.getName(), UTF8).equals(USERID)) {
// marker.setUserid(new String(col.getValue(), UTF8));
// CommonUtil.tolog("marker.getUserid()  = " + marker.getUserid());
// }
// else if (new String(col.getName(), UTF8).equals(ROWKEY)) {
// marker.setRowKey(new
// String(UUID.nameUUIDFromBytes(col.getValue()).toString()));
// CommonUtil.tolog("marker.getRowKey()  = " + marker.getRowKey());
// }
// else if (new String(col.getName(), UTF8).equals(LATITUDE)) {
// marker.setLatitude(ByteBuffer.wrap(col.getValue()).getDouble());
// CommonUtil.tolog("marker.getLatitude()  = " + marker.getLatitude());
// }
// else if (new String(col.getName(), UTF8).equals(LONGTITUDE)) {
// marker.setLongtitude(ByteBuffer.wrap(col.getValue()).getDouble());
// CommonUtil.tolog("marker.getLongtitude()  = " + marker.getLongtitude());
// }
// else if (new String(col.getName(), UTF8).equals(ALTITUDE)) {
// marker.setAltitude(ByteBuffer.wrap(col.getValue()).getDouble());
// CommonUtil.tolog("marker.getAltitude()  = " + marker.getAltitude());
// }
// else if (new String(col.getName(), UTF8).equals(AREA)) {
// marker.setArea(ByteBuffer.wrap(col.getValue()).getDouble());
// CommonUtil.tolog("marker.getArea()  = " + marker.getArea());
// }
// else if (new String(col.getName(), UTF8).equals(TITLE)) {
// marker.setTitle(new String(col.getValue(), UTF8));
// CommonUtil.tolog("marker.getTitle()  = " + marker.getTitle());
// }
// else if (new String(col.getName(), UTF8).equals(MEMO)) {
// marker.setMemo(new String(col.getValue(), UTF8));
// CommonUtil.tolog("marker.getMemo()  = " + marker.getMemo());
// }
// else if (new String(col.getName(), UTF8).equals(PICDATA)) {
//
// byte[] picdata = col.getValue();
// Bitmap picBM = BitmapFactory.decodeByteArray(picdata, 0, picdata.length);
// // picBM =
// // BitmapFactory.decodeResource(context.getResources(),
// // R.drawable.btn_star_big_on);
// marker.setPic(picBM);
// }
// else {
// // new Throwable();
// }
// }
//
// CommonUtil.tolog("pic length = " + marker.getPic().getByteCount());
// tempMarkers.add(marker);
//
// }
//
// // add n remove markers
// CommonUtil.tolog("tempMarkers size = " + tempMarkers.size());
// }
// catch (Exception e) {
// // TODO: handle exception
// e.printStackTrace();
// }
// //
// // else {
// //
// // for (Iterator<MarkerData> iterator = tempMarkers.iterator();
// // iterator.hasNext();) {
// // MarkerData tempMarker = (MarkerData) iterator.next();
// //
// // for (Iterator<MarkerData> iterator2 = this.markers.iterator();
// // iterator2.hasNext();) {
// // MarkerData originMarker = (MarkerData) iterator2.next();
// //
// // if (tempMarker.getRowKey().equals(originMarker.getRowKey())) {
// // add_flag = 1;
// // }
// //
// // }
// //
// // if (add_flag != 1) {
// // this.markers.add(tempMarker);
// // getAugViewHandler().addview(tempMarker);
// // }
// // add_flag = 0;
// //
// // }
// // }
// //
// // printMaers();
// //
// // for (Iterator<MarkerData> iterator3 = this.markers.iterator();
// // iterator3.hasNext();) {
// // MarkerData originMarker = (MarkerData) iterator3.next();
// //
// // for (Iterator<MarkerData> iterator = tempMarkers.iterator();
// // iterator.hasNext();) {
// // MarkerData tempMarker = (MarkerData) iterator.next();
// //
// // // if (originMarker == null) {
// // // CommonUtil.tolog("originMarker  is null");
// // // }
// // // if (tempMarker == null) {
// // // CommonUtil.tolog("tempMarker  is null");
// // // }
// // // if (originMarker.getRowKey() == null) {
// // // CommonUtil.tolog("originMarker rowkey is null");
// // // }
// // // if (tempMarker.getRowKey() == null) {
// // // CommonUtil.tolog("tempMarker rowkey is null");
// // // }
// //
// // if (originMarker.getRowKey().equals(tempMarker.getRowKey())) {
// // remove_flag = 1;
// // }
// //
// // }
// // // CQL 의 marker가 현재 핸들러에 포함되어 있지 않다면
// // if (remove_flag != 1) {
// // this.markers.remove(originMarker);
// // getAugViewHandler().removeView(originMarker);
// // }
// // remove_flag = 0;
// // }
// }

// ///////////// insert image

// Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),
// com.ssm.skycanvas.activity.R.drawable.ic_launcher2);
// ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
// bitmap.compress(CompressFormat.PNG, 100, byteArray);
// byte[] bytes = byteArray.toByteArray();
//
// Column description2 = new Column();
// description2.setName("picdata".getBytes());
// description2.setValue(bytes);
// description2.setTimestamp(System.currentTimeMillis());
// ConsistencyLevel consistencyLevel = ConsistencyLevel.ONE;
//
// try {
// client.insert(ByteBuffer.wrap(asByteArray(UUID.fromString("c3f99ef2-9d0b-4c40-93ca-2d8b062e0f33"))),
// parent, description2, consistencyLevel);
// }
// catch (InvalidRequestException e1) {
// // TODO Auto-generated catch block
// e1.printStackTrace();
// }
// catch (UnavailableException e1) {
// // TODO Auto-generated catch block
// e1.printStackTrace();
// }
// catch (TimedOutException e1) {
// // TODO Auto-generated catch block
// e1.printStackTrace();
// }
// catch (TException e1) {
// // TODO Auto-generated catch block
// e1.printStackTrace();
// }
