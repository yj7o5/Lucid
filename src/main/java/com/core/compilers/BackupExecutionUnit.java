import java.io.File;
import java.io.IOException;

//This will only work on Windows and Linux (that has xterm)
public class Run {
	
	private static File directory; //Directory that contains the class files to be run.
	private static String filePath = directory.getPath(); 
	
	Run(File dir)
	{
		directory = dir;
	}
	
	//We can assume that there is a Main.java, therefore only the Main.java needs to be ran if all files are compiled.
	public static void execute()
	{
		boolean flag = false;
		
		//Windows
		//Assuming I am right about how try/catch works, this setup should work to try for both Windows and Linux.
		try
        { 
			//It is probably necessary to cd to the directory that the files are in before running the javac and java commands
			String command = "cmd /c start cmd.exe /K \"javac *.java && java" + filePath + " Tester\"";
            Runtime.getRuntime().exec(command); 
            

        } 
        catch (Exception e) 
        { 
        	System.out.println("There was an error running the cmd commands, attempting Linux version...");
            flag = true;
        }
		
		//Linux
		if(flag == true)
		{
			try
			{
			//Also *.java didn't seem to work using this method for some reason, but the files should already be compiled before running this, so
			//that part can just be commented out.
			String command1[] = {"xterm", "-e", "javac", filePath ," *.java"}; //command1 is only necessary if files are not yet compiled
			String command2[] = {"xterm", "-hold", "-e", "java", "-cp", filePath, "Main"};
			
			Process proc = Runtime.getRuntime().exec(command1);
			proc.waitFor();
			
			Runtime.getRuntime().exec(command2);
			}
			catch(Exception e)
			{
				System.out.println("There was an error running the terminal commands");
				e.printStackTrace(); 
			}
		}
	}
	

	public static void main(String[] args) throws IOException, InterruptedException
	{
		execute();
	}
		

}
