package ro.pub.cs.systems.pdsd.practicaltest02;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class PracticalTest02MainActivity extends Activity {

	Button submit_button;
	EditText autocomplete_field;
	EditText results_field;
	ServerThread serverThread;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_practical_test02_main);
		serverThread = new ServerThread();
		serverThread.startServer();
		autocomplete_field = (EditText) findViewById(R.id.autocomplete_field);
		results_field = (EditText) findViewById(R.id.results_field);
		submit_button = (Button) findViewById(R.id.submit_button);
		submit_button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				System.out.println("s-a dat click");
				new ClientThread().start();
			}
		});

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

		serverThread.stopServer();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.practical_test02_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private class ServerThread extends Thread {

		private boolean isRunning;

		private ServerSocket serverSocket;

		public void startServer() {
			isRunning = true;
			start();
		}

		public void stopServer() {
			isRunning = false;
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						if (serverSocket != null) {
							serverSocket.close();
						}
						Log.v(Constants.TAG, "stopServer() method invoked "
								+ serverSocket);
					} catch (IOException ioException) {
						Log.e(Constants.TAG, "An exception has occurred: "
								+ ioException.getMessage());
						if (Constants.DEBUG) {
							ioException.printStackTrace();
						}
					}
				}
			}).start();
		}

		@Override
		public void run() {
			try {
				serverSocket = new ServerSocket(Constants.SERVER_PORT);
				while (isRunning) {
					Socket socket = serverSocket.accept();
					System.out.println("conectare socket");
					PrintWriter printWriter = Utilities.getWriter(socket);
					BufferedReader br = Utilities.getReader(socket);
					String autocomplete_value = br.readLine();
					HttpClient httpClient = new DefaultHttpClient();
					System.out.println("Query este: " + autocomplete_value);
					HttpGet httpGet = new HttpGet(
							"http://autocomplete.wunderground.com/aq?query="
									+ autocomplete_value);
					HttpResponse httpGetResponse = httpClient.execute(httpGet);
					HttpEntity httpGetEntity = httpGetResponse.getEntity();
					String results = EntityUtils.toString(httpGetEntity);
					System.out.println("Rezultate " + results);
					printWriter.write(results);
					printWriter.flush();
					final String results_final = results;
					results_field.post(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							autocomplete_field.setText(results_final);
						}
					});
					socket.close();
					Log.v(Constants.TAG, "Connection closed");
				}
			} catch (IOException ioException) {
				Log.e(Constants.TAG, "An exception has occurred: "
						+ ioException.getMessage());
				if (Constants.DEBUG) {
					ioException.printStackTrace();
				}
			}
		}
	}

	class ClientThread extends Thread {

		private Socket socket;

		public ClientThread() {
		}

		@Override
		public void run() {
			try {
				socket = new Socket("localhost", Constants.SERVER_PORT);
				if (socket == null) {
					Log.e(Constants.TAG,
							"[CLIENT THREAD] Could not create socket!");
				}

				BufferedReader bufferedReader = Utilities.getReader(socket);
				PrintWriter printWriter = Utilities.getWriter(socket);
				if (bufferedReader != null && printWriter != null) {
					printWriter.write(autocomplete_field.getText().toString());
					String result = null;
					result = bufferedReader.readLine();
					final String results_a = result;
					results_field.post(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							results_field.setText(results_a);
						}
					});
				} else {
					Log.e(Constants.TAG,
							"[CLIENT THREAD] BufferedReader / PrintWriter are null!");
				}
				socket.close();
			} catch (IOException ioException) {
				Log.e(Constants.TAG,
						"[CLIENT THREAD] An exception has occurred: "
								+ ioException.getMessage());
				if (Constants.DEBUG) {
					ioException.printStackTrace();
				}
			}
		}

	}
}
