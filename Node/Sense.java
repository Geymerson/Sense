import static java.lang.System.out;
import net.tinyos.message.*;
import net.tinyos.util.*;
import net.tinyos.packet.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

class Sense implements MessageListener{
	private PhoenixSource phoenix;
	private MoteIF mif;
	private File file;
	private FileOutputStream foStream;
	private Date date;
		
	public Sense(final String source){
		phoenix = BuildSource.makePhoenix(source, PrintStreamMessenger.err);
		mif = new MoteIF(phoenix);
		mif.registerListener(new SenseMsg(),this);
		this.date = new Date();
		try {
			file = new File("Sampled_Data.txt");
			foStream = new FileOutputStream(file, true);
			foStream.write("Node".getBytes());
			foStream.write("\t ".getBytes());
			foStream.write("Temp".getBytes());
			foStream.write("\t\t ".getBytes());
			foStream.write("Humi".getBytes());
			foStream.write("\t".getBytes());
			foStream.write("Itr_Pres".getBytes());
			foStream.write("\t".getBytes());
			foStream.write("Itr_Temp".getBytes());
			foStream.write("\t".getBytes());
			foStream.write("IR_Light".getBytes());
			foStream.write("\t".getBytes());
			foStream.write("Vis_Light".getBytes());
			foStream.write("\t".getBytes());
			foStream.write("Accel_X".getBytes());
			foStream.write("\t".getBytes());
			foStream.write("Accel_Y".getBytes());
			foStream.write("\t\t".getBytes());			
			foStream.write("Voltage".getBytes());
			foStream.write("\t\t\t\t\t".getBytes());
			foStream.write("Date".getBytes());
			foStream.write("\n".getBytes());
		} catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	public void messageReceived(int dest_addr, Message msg){
		if(msg instanceof SenseMsg){
			SenseMsg results = (SenseMsg)msg;
			int[] taosCalcData = null;
			double[] sensirionCalcData=null;
			int voltage;
			out.println("The measured results are:");
			out.println();
			out.println("Node:                   "+results.get_nodeid());
			voltage = (1223 * 1024)/results.get_Voltage_data();
			out.println("Voltage:                "+voltage);
			out.println("Accelerometer X axis:   "+results.get_AccelX_data());
			out.println("Accelerometer Y axis:   "+results.get_AccelY_data());
			out.println("Intersema temperature:  "+results.getElement_Intersema_data(0));
			out.println("Intersema pressure:     "+results.getElement_Intersema_data(1));
			sensirionCalcData=calculateSensirion(results.get_Temp_data(),results.get_Hum_data());
			out.printf("Sensirion temperature:  %.2f\n",sensirionCalcData[0]);
			out.printf("Sensirion humidity:     %.2f\n",sensirionCalcData[1]);
			taosCalcData=calculateTaos(results.get_VisLight_data(),results.get_InfLight_data());
			out.println("Taos visible light:     "+taosCalcData[0]);
			out.println("Taos infrared light:    "+taosCalcData[1]);
			out.println();
			try{
				//Node ID
				foStream.write(String.valueOf(results.get_nodeid()).getBytes());
				foStream.write("\t\t".getBytes());
				
				//Sensirion temperature
				//sensirionCalcData=calculateSensirion(results.get_Temp_data(),results.get_Hum_data());
				foStream.write(String.valueOf(String.format("%.2f", sensirionCalcData[0])).getBytes());
				foStream.write("\t\t".getBytes());
				
				//Sensirion humidity
				foStream.write(String.valueOf(String.format("%.2f", sensirionCalcData[1])).getBytes());
				foStream.write("\t ".getBytes());
				
				//Intersema pressure
				foStream.write(String.valueOf(String.format("%.1f", (float)(results.getElement_Intersema_data(1)/10))).getBytes());
				foStream.write("\t\t ".getBytes());
				
				//Intersema temperature
				foStream.write(String.valueOf(String.format("%.1f", (float)(results.getElement_Intersema_data(0)/10))).getBytes());
				foStream.write("\t\t ".getBytes());
				
				//Infrared light
				//taosCalcData=calculateTaos(results.get_VisLight_data(),results.get_InfLight_data());
				foStream.write(String.valueOf(String.format("%04d", taosCalcData[1])).getBytes());
				foStream.write("\t\t  ".getBytes());
				
				//Visible light
				foStream.write(String.valueOf(String.format("%04d", taosCalcData[0])).getBytes());
				foStream.write("\t\t ".getBytes());
				
				//Accel_X
				foStream.write(String.valueOf(String.format("%04d", (int)results.get_AccelX_data())).getBytes());
				foStream.write("\t ".getBytes());
				
				//Accel_Y
				foStream.write(String.valueOf(String.format("%04d", (int)results.get_AccelY_data())).getBytes());
				foStream.write("\t\t ".getBytes());

				//Voltage
				foStream.write(String.valueOf(voltage).getBytes());
				foStream.write("\t\t".getBytes());			
				
				//Date
				this.date.setTime(System.currentTimeMillis());
				foStream.write(this.date.toString().getBytes());
				foStream.write("\n".getBytes());
			}
			catch(Exception ex) {
				ex.printStackTrace();
			}
		}
			
	}
	
	private int[] calculateTaos(int VisibleLight,int InfraredLight) {
		final int CHORD_VAL[]={0,16,49,115,247,511,1039,2095};
		final int STEP_VAL[]={1,2,4,8,16,32,64,128};
		int chordVal,stepVal;
		int[] lightVal=new int[2];
		
		chordVal=(VisibleLight>>4) & 7;
		stepVal=VisibleLight & 15;
		lightVal[0]=CHORD_VAL [chordVal]+stepVal*STEP_VAL[chordVal];
		chordVal=(InfraredLight>>4)&7;
		stepVal=VisibleLight & 15;
		lightVal[1]=CHORD_VAL[chordVal]+stepVal*STEP_VAL[chordVal];
		return lightVal;
	}
	
	private double[] calculateSensirion(int Temperature,int Humidity){
		double [] converted = new double[2]; 
		
		converted[0]=-39.4+(0.01*(double)Temperature);
		converted[1]=(-2.0468+0.0367*(double)Humidity-0.0000015955*Math.pow((double)Humidity,(double )2))+(converted[0]-25)*(0.01+0.00008*(double)Humidity);
			
		return converted;
	}
	
	public static void main (String[] args) {
		if ( args.length == 2 && args[0].equals("-comm") ) {
			Sense sense = new Sense(args[1]);
		} else {
			System.err.println("usage: java Sense [-comm <source>]");
			System.exit(1);
		}
	}
}
