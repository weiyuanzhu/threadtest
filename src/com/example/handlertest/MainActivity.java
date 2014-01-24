package com.example.handlertest;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity  implements Slaver.CallBack{
	
	static final int UART_STOP_BIT_H = 0x5A;
	static final int UART_STOP_BIT_L = 0xA5;
	static final int UART_NEW_LINE_H = 0x0D;
	static final int UART_NEW_LINE_L = 0x0A;

	private List<Integer> rxBuffer = null;
	private List<List<Integer>> eepRom = null;
	
	private Handler rxHandler = null;
	
	private boolean rxComplete = false;
	private Slaver slaver;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		rxBuffer = new ArrayList<Integer>();
		
		slaver = new Slaver(this);
		
		slaver.pullData(this);
		
		rxHandler = new Handler()
		{

			@Override
			public void handleMessage(Message msg) {
				
				System.out.println("parsing rx ------------");
			}
			
			
		};
		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void receive(List<Integer> rx) {
		
		rxBuffer = new ArrayList<Integer>(rx);

		slaver.setIsClosed(true);
				
		if(this.rxBuffer.size()< 16500)
		{
			
			System.out.println("not complete " + this.rxBuffer.size());
			slaver.pullData(this);
					
		}
		else {
			
			//rxComplete = true;
			new Thread(parse).start();
		}
		

		System.out.println("Actual bytes received: " + rxBuffer.size());
		
	}
	
	public void click(View v)
	{
		slaver.pullData(this);
		
		
	}
	
	Runnable parse = new Runnable()
	{

		@Override
		public void run() {
			
			eepRom = new ArrayList<List<Integer>>();
			
			System.out.println("Analyzing data from new thread");
			System.out.println("rxBuffer.size = " + rxBuffer.size());
			
			for (int i=0,j=0; i<rxBuffer.size(); i++)
			{
				if((rxBuffer.get(i) == UART_NEW_LINE_L) && 
        				rxBuffer.get(i - 1)==(UART_NEW_LINE_H) &&
        				rxBuffer.get(i - 2)==(UART_STOP_BIT_L) &&
        				rxBuffer.get(i - 3)==(UART_STOP_BIT_H))
				{
						
					eepRom.add(rxBuffer.subList(j, i));	
					j = i+1;
				}
				
				
			}
			
			System.out.println(eepRom.size());
			
		}
		
		
	};

}
