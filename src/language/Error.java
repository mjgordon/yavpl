package language;

public class Error {
	ErrorType errorType;
	
	public Error(ErrorType errorType) {
		this.errorType = errorType;
		
		System.out.println("New Error : " + this.errorType);
	}
	
}
