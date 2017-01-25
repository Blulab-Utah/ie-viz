package edu.utah.blulab.commandline;


public class CommandLineException extends Exception {
	public String message = null;
	static final long serialVersionUID = 0;
	
	public CommandLineException(String msg) {
		super(msg);
		this.message = msg;
	}
	
	@Override
    public String getMessage(){
       return this.message;
    }

}
