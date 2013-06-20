package com.colobu.flyrunner.view;

public class FunctionInfo
{
	private String name;  
	private int resId;
    
   	public FunctionInfo(String name,int resId) {   
        this.name = name;  
        this.resId = resId; 
    }  
  
    public String getName() {  
        return name;  
    }  
  
    public void setName(String name) {  
        this.name = name;  
    }  
    
    public int getResId()
   	{
   		return resId;
   	}

   	public void setResId(int resId)
   	{
   		this.resId = resId;
   	}
}
