package com.oldratlee.cooma.internal.bytecode;

/**
 * NoSuchPropertyException.
 * 
 * @author qian.lei
 */

public class NoSuchPropertyException extends RuntimeException
{
	private static final long serialVersionUID = -2725364246023268766L;

	public NoSuchPropertyException()
	{
		super();
	}

	public NoSuchPropertyException(String msg)
	{
		super(msg);
	}
}