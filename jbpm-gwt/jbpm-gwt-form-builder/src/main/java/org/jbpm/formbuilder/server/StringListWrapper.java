package org.jbpm.formbuilder.server;

import java.util.List;
/*
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "wrapper")*/
public class StringListWrapper {

    List<String> stringList;
    
    public StringListWrapper() {
    }
    
    public StringListWrapper(List<String> stringList) {
        this.stringList = stringList;
    }
    
  /*  @XmlElement
    public List<String> getStringList() {
        return stringList;
    }
    
    @XmlElement(name = "totalCount")
    public int getTotalCount()
    {
      return this.stringList.size();
    }
    
    public void setStringList(List<String> stringList) {
        this.stringList = stringList;
    }*/
}
