import java.io.IOException;

public class Run {
	
	public static void run()
	{
		boolean flag = false;
		
		//Windows
		try
        { 
			//It is probably necessary to cd to the directory that the files are in before running the javac and java commands
           Runtime.getRuntime().exec("cmd /c start cmd.exe /K \"javac *.java && java Main\""); 
        } 
        catch (Exception e) 
        { 
            flag = true;
        }
		
		//Linux
		if(flag == true)
		{
			try
			{
			//src needs to be replaced in both the commands below with the path to the file being compiled/run.
			//Also *.java doesn't work using this method for some reason, but the files should already be compiled before running this, so
			//that part can just be commented out.
			String command1[] = {"xterm", "-e", "javac", "src/*.java"}; //command1 is only necessary if files are not yet compiled
			String command2[] = {"xterm", "-hold", "-e", "java", "-cp", "src", "Main"};
			
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
		run();
	}
		

}
