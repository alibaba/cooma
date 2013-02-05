package com.metaframe.cooma.internal.bytecode;

/**
 * NoSuchMethodException.
 * 
 * @author qian.lei
 */

public class NoSuchMethodException extends RuntimeException
{
	private static final long serialVersionUID = -2725364246023268766L;

	public NoSuchMethodException()
	{
		super();
	}

	public NoSuchMethodException(String msg)
	{
		super(msg);
	}
}