package Tux2.TuxTwoLib;

import java.util.UUID;

public class NMSHeadData {
	private UUID uuid;
	private String texture;
	
	public NMSHeadData(UUID uuid, String texture){
		this.uuid = uuid;
		this.texture = texture;
	}
	
	public void setId(UUID uuid){
		this.uuid = uuid;
	}
	
	public void setTexture(String texture){
		this.texture = texture;
	}
	
	public UUID getId(){
		return this.uuid;
	}
	
	public String getTexture(){
		return this.texture;
	}
}
