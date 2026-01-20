package botsserver;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 *
 * @author BoutEagle
 */

public class Packet {

    private ByteBuffer packet = ByteBuffer.allocate(2000);
    private ByteBuffer header = ByteBuffer.allocate(4);
    private int packlen = 0;
    private int readstart = 0;
    private boolean calced = false;
    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    
    public Packet(){
    	header.order(java.nio.ByteOrder.LITTLE_ENDIAN);
    	packet.order(java.nio.ByteOrder.LITTLE_ENDIAN);
    }
    
    protected void debug(String msg) {
        Main.debug("Packet: "+ msg);
    }
    
    //Header
    public void addHeader(byte b1, byte b2){
        calced = false;
        byte[] headbyte = {b1, b2};
        header.put(headbyte);
    }
    
    protected void calcHeader()
    {
        header.putShort((short)this.packlen);
        calced = true;
    }
    
    public int getreadStart()
    {
    	return readstart;
    }
    
    //THIS GETS THE DATA TO BE SENT
    public byte[] getHeader()
    {
        if (!calced)
            this.calcHeader();
        try
        {
        //byte[] packb = this.header.getBytes("ISO8859-1");
        return this.header.array(); //new String(packb,"ISO8859-1");
        } catch (Exception e)
        {
        }
        return null;
    }
    
    //packet body
    public void removeHeader()
    {
    	//header = ByteBuffer.allocate(4);
    	//header.order(java.nio.ByteOrder.LITTLE_ENDIAN);
    	//readstart+=4;
    	byte[] save=Arrays.copyOfRange(packet.array(),4,packlen);
    	packet=ByteBuffer.allocate(2000);
    	packet.put(save);
    	packlen-=4;
    }
    
    public int getPackSize()
    {
    	return header.getShort(2);
    }
    
    public int getPackSizeRelay()
    {
    	return packet.getShort(2);
    }
    
    public int getLen()
    {
        return packlen;
    }

    public void setPacket(byte[] pack)
    {
    	clean();
        this.packet.put(pack);
        packlen+=pack.length;
    }
    
    public void setPacketarray(Packet pack)
    {
        try {
        	byte[] temp = pack.getPacket();
        	packet = ByteBuffer.allocate(10000);
			this.packet.put(temp);
	        packlen=temp.length;
		} catch (Exception e) {}
    }

    public void addPacketHead(byte b1, byte b2)
    {
        try
        {
        	byte[] two = {b1,b2};
        	packet.put(two);
        	packlen+=2;
        }
        catch (Exception e) {
        }
    }


    public void addString(String string)
    {
    	try{
	    	packet.put(string.getBytes("ISO8859-1"));
	    	packlen+=string.length();
    	}catch (Exception e){}
    }

    public String getString(int start, int end, boolean nulled)
    {
    	byte[] string = new byte[end-start];
    	string=Arrays.copyOfRange(packet.array(), start+readstart, end+readstart);
        String dony="";
		try {
			dony = new String(string,"ISO8859-1");
		} catch (Exception e) {}
        //debug(dony);
        readstart+=end;
        if(!nulled)
        	return removenullbyte(dony, string);
        else	
        	return dony;
    }
    
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    protected String removenullbyte(String thestring, byte[] stringbyte)
    {
        try
        {
        //byte[] stringbyte = thestring.getBytes("ISO8859-1");
        //debug(bytesToHex(stringbyte));
        int a = 0;
        while(stringbyte[a] != (byte)0x00)
            a++;

        return thestring.substring(0, a);
        } catch (Exception e){

        }
        return null;
    }

    public byte[] getasByte(int var, int num)
    {
        try
        {
        if(num == 2){
            int b1 = var & 0xff;
            int b2 = (var >> 8) & 0xff;
            byte[] varbyte = {(byte)b1, (byte)b2};
            return varbyte;//new String(varbyte,"ISO8859-1");

        } else if(num == 4){
            int b1 = var & 0xff;
            int b2 = (var >> 8) & 0xff;
            int b3 = (var >> 16) & 0xff;
            int b4 = (var >> 24) & 0xff;
            byte[] varbyte = {(byte)b1, (byte)b2, (byte)b3, (byte)b4};
            return varbyte;//new String(varbyte,"ISO8859-1");
        }
        } catch (Exception e)
        {
        }
        return null;

    }




    public void addInt(int var, int num, boolean reverse)
    {
        try{
        	if(reverse){
        		if(num==1)
        			packet.put((byte)var);
        		if(num==2)
        			packet.putShort((short)(((var<<8)&0xff00) | ((var>>8)&0xff)));
        		if(num==4)
        			packet.putInt(((var>>24)&0xff) | // move byte 3 to byte 0
                    ((var<<8)&0xff0000) | // move byte 1 to byte 2
                    ((var>>8)&0xff00) | // move byte 2 to byte 1
                    ((var<<24)&0xff000000));
        		packlen+=num;
        	}
        	else{
        		if(num==1)
        			packet.put((byte)var);
        		if(num==2)
        			packet.putShort((short)var);
        		if(num==4)
        			packet.putInt(var);
        		packlen+=num;
        	}
        }
        catch (Exception e) {
        }
    }

    
    
    public int getInt(int bytec)
    {
    	int ret = 0;
    	byte[] newarray = new byte[bytec];
    	newarray = Arrays.copyOfRange(packet.array(), readstart, readstart+bytec);
    	for(int i = 0; i<bytec; i++)
    		if(i==0)
    			ret+=newarray[i] & 0xFF;
    		else
    			ret+=(newarray[i] & 0xFF) << (8*i);
    	this.readstart+=bytec;
    	return ret;
    }

    public void addByte(byte b1)
    {
        try
        {
	        packet.put(b1);
	        packlen+=1;
        }catch (Exception e){}
    }

    public void addByte2(byte b1, byte b2)
    {
        try
        {
        	byte[] two = {b1, b2};
        	packet.put(two);
        	packlen+=2;
        }
        catch (Exception e) {
        }
    }

    public void addByte4(byte b1, byte b2, byte b3, byte b4)
    {
        try
        {
        	byte[] two = {b1, b2, b3, b4};
	        packet.put(two);
	        packlen+=4;
        }
        catch (Exception e) {
        }
    }

    public void addByteArray(byte[] two)
    {
        try
        {
        	packet.put(two);
        	packlen+=two.length;
        }
        catch (Exception e) {
        	debug(""+e);
        }
    }
    
    public byte[] getPacket()
    {
        try 
        {
        	return Arrays.copyOfRange(this.packet.array(),0,packlen);
        } catch (Exception e)
        {
        }
        return null;
    }

    public void clean(){
        this.header = ByteBuffer.allocate(4);
        this.packet = ByteBuffer.allocate(2000);
        packlen=0;
        readstart=0;
        header.order(java.nio.ByteOrder.LITTLE_ENDIAN);
    	packet.order(java.nio.ByteOrder.LITTLE_ENDIAN);
    }
    
    public void cleanheader(){
    	this.header = ByteBuffer.allocate(4);
    	header.order(java.nio.ByteOrder.LITTLE_ENDIAN);
    }
    public void cleanpacket() {
    	this.packet = ByteBuffer.allocate(2000);
    	packlen=0;
    	packet.order(java.nio.ByteOrder.LITTLE_ENDIAN);
    }
    

}
