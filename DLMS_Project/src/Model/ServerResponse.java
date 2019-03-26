package Model;

import java.io.Serializable;

public class ServerResponse implements Serializable  {
	
	private String replicaName ;
	private String result ;
	
	public ServerResponse ( String newReplicaName, String newResult ) {
		replicaName = newReplicaName ;
		result = newResult ;
	}
	
	/**
	 * @return the replicaName
	 */
	public String getReplicaName() {
		return replicaName;
	}
	
	/**
	 * @return the result
	 */
	public String getResult() {
		return result;
	}
}