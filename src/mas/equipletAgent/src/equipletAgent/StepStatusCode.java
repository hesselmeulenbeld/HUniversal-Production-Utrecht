package equipletAgent;

public enum StepStatusCode {
	EVALUATING(0),
	PLANNED(1),
	WAITING(2),
	IN_PROGRESS(3),
	SUSPENDED_OR_WARNING(4),
	DONE(5),
	ABORTED(6),
	FAILED(7);

	private int status;
	
	private StepStatusCode(int status){
		this.status = status;
	}
	
	public int getStatus(){
		return status;
	}
}