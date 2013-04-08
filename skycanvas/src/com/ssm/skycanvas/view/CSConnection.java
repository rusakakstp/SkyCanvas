package com.ssm.skycanvas.view;

public class CSConnection {
//	package com.example.skycanvas;
//
//	import java.io.ByteArrayOutputStream;
//	import java.nio.ByteBuffer;
//
//	import org.apache.cassandra.thrift.Cassandra;
//	import org.apache.cassandra.thrift.Column;
//	import org.apache.cassandra.thrift.ColumnOrSuperColumn;
//	import org.apache.cassandra.thrift.ColumnParent;
//	import org.apache.cassandra.thrift.Compression;
//	import org.apache.cassandra.thrift.ConsistencyLevel;
//	import org.apache.cassandra.thrift.CqlMetadata;
//	import org.apache.cassandra.thrift.CqlResult;
//	import org.apache.cassandra.utils.ByteBufferUtil;
//	import org.apache.cassandra.utils.Hex;
//	import org.apache.thrift.protocol.TBinaryProtocol;
//	import org.apache.thrift.protocol.TProtocol;
//	import org.apache.thrift.transport.TFramedTransport;
//	import org.apache.thrift.transport.TSocket;
//	import org.apache.thrift.transport.TTransport;
//
//	import android.app.Activity;
//	import android.graphics.Bitmap;
//	import android.graphics.Bitmap.CompressFormat;
//	import android.graphics.BitmapFactory;
//	import android.graphics.drawable.Drawable;
//	import android.os.Bundle;
//	import android.util.Log;
//	import android.view.Menu;
//	import android.widget.ImageView;
//
//	public class MainActivity extends Activity {
//		public static String clusterName = "Test Cluster";
//		public static String keySpaceName = "TESTDB";
//		public static String columnFamilyName = "MEMBER";
//		public static String HOST = "121.177.66.251";
//		public static int PORT = 9160;
//		private static final String UTF8 = "UTF8";
//		private CqlResult cql;
//
//		@Override
//		protected void onCreate(Bundle savedInstanceState) {
//			super.onCreate(savedInstanceState);
//			setContentView(R.layout.activity_main);
//
//			TTransport transport = new TSocket(HOST, PORT);
//			TFramedTransport framedTransport = new TFramedTransport(transport);
//			TProtocol protocol = new TBinaryProtocol(framedTransport);
//			Cassandra.Client client = new Cassandra.Client(protocol);
//			try {
//				framedTransport.open();
//				client.set_keyspace(keySpaceName);
//
//				byte file;
//
//				Drawable draw = getResources().getDrawable(R.drawable.ic_launcher);
//				Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
//				ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
//				bitmap.compress(CompressFormat.PNG, 100, byteArray);
//				byte[] bytes = byteArray.toByteArray();
////				file = Byte.parseByte(byteArray.toString());
////				ByteBuffer buffer = ByteBufferUtil.bytes(file);
//				
//				  // define column parent
//		        ColumnParent parent = new ColumnParent("test");
//
//		        // define row id
//		        ByteBuffer rowid = ByteBuffer.wrap("pic1".getBytes());
//
//		        // define column to add
//		        Column description = new Column();
//		        description.setName("id".getBytes());
//		        description.setValue("123".getBytes());
//		        description.setTimestamp(System.currentTimeMillis());
//		        ColumnOrSuperColumn aef = new ColumnOrSuperColumn();
//		        
//		        //	        
//	//
////		        Column description2 = new Column();
////		        description.setName("pic".getBytes());
////		        description.setValue(bytes);
////		        description.setTimestamp(System.currentTimeMillis());
//	//
////		        // define consistency level
//		        ConsistencyLevel consistencyLevel = ConsistencyLevel.ONE;
////				
////		        // execute insert
////		        client.insert(rowid, parent, description, consistencyLevel);
////		        client.insert(rowid, parent, description2, consistencyLevel);
//		        
//		        // release resources
////		        transport.flush();
////		        transport.close();
//		        
//
////				client.execute_cql_query(new ByteBufferUtil().bytes("INSERT INTO test(KEY,id,pic) VALUES('pic1','123'," + byteArray.toString() + ")"), Compression.NONE);
////				cql = client.execute_cql_query(new ByteBufferUtil().bytes("SELECT * from MEMBER"), Compression.NONE);
//				cql = client.execute_cql_query(new ByteBufferUtil().bytes("SELECT * from test"), Compression.NONE);
//				// CqlResult cq2l = client.execute_cql_query(new
//				// ByteBufferUtil().bytes("INSERT INTO MEMBER (KEY,age,gender) VALUES('member3','25','남자');"),
//				// Compression.NONE);
//				
//				tolog(new String(cql.rows.get(0).columns.get(0).getValue()));
//				byte bytes2[] = cql.rows.get(0).columns.get(1).getValue();
//				Bitmap bitmap2 = BitmapFactory.decodeByteArray(bytes2, 0, bytes2.length);
//				
//				ImageView image = (ImageView)findViewById(R.id.imageView1);
//				image.setImageBitmap(bitmap2);
//				
//				
////				String age = new String(cql.rows.get(0).getColumns().get(1).getName(), "UTF-8");
////				String value = new String(cql.rows.get(0).getColumns().get(2).getName(), "UTF-8");
////				int rowSize = cql.rows.size();
////				int columnSize = cql.rows.get(0).getColumnsSize();
////				tolog(rowSize);
////				tolog(columnSize);
////				Log.i("test", "test");
//	//
////				for (int i = 0; i < rowSize; i++) {
////					tolog("row " + i);
////					for (int j = 0; j < columnSize; j++) {
////						tolog(new String(cql.rows.get(i).getColumns().get(j).getName(), UTF8) + " : ");
////						tolog(new String(cql.rows.get(i).getColumns().get(j).getValue(), UTF8));
////					}
////				}
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//
//		@Override
//		public boolean onCreateOptionsMenu(Menu menu) {
//			// Inflate the menu; this adds items to the action bar if it is present.
//			getMenuInflater().inflate(R.menu.activity_main, menu);
//			return true;
//		}
//
//		public void tolog(String str) {
//			Log.i("@@@---my---@@@", str);
//		}
//
//		public void tolog(int value) {
//			Log.i("@@@---my---@@@", Integer.toString(value));
//		}
//
//	}

}
