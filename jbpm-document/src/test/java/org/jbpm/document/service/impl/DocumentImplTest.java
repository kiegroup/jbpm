package org.jbpm.document.service.impl;

import org.jbpm.document.Document;
import org.junit.Assert;
import org.junit.Test;

public class DocumentImplTest {

	@Test
	public void testToStringRepresentation(){
		
		Document document = new DocumentImpl();
		try{
			Assert.assertNotNull( document.toString() );
		}catch( Throwable th ){
			Assert.fail( "toString method must not fire any exception:" + th.getMessage() );
		}
	}
}
