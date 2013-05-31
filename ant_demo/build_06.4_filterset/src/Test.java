
/**
 * @author %author! 
 * @date@
 * 
 * 
 * 经Ant filterset 处理后，复制后的文件@author值将显示真正的开发者名。
 * @date@则被替换成真正的日期。
 * */
public class Test {
	private static int LIMIT_SET_DISCUSSION_NAME =48;

	public static void main(String[]args){
        try{
            String line = "咑匴咑匴咑匴咑咑匴咑匴咑、C、华仔";
			byte[]dataBytes = line.getBytes("utf-8");
            if (dataBytes.length > LIMIT_SET_DISCUSSION_NAME) {
                int charNum = line.length();
                for (int i = 1; i <= charNum; i++) {
                    String tmpLine = line.substring(0, i);
                    byte[] tmpBytes = tmpLine.getBytes("utf-8");
                    if(tmpBytes.length >LIMIT_SET_DISCUSSION_NAME ){
                        line = line.substring(0, i-1);
                        break;
                    }
                }
            }
            System.out.println("line="+line+"\nbyte.len="+line.getBytes("utf-8").length);
            
            int charNum = line.length();
            System.out.println(charNum+ " [0,1]="+line.substring(0,16));
        }catch(Exception e){
            e.printStackTrace();
        }
	}
}
