//load Test set up script
import jxl.*
import jxl.write.*
def CMMACList=[]
Workbook workbook = Workbook.getWorkbook(new File("E:/CMMAC.xls"))
Sheet sheet = workbook.getSheet(0)     
for(int i=0;i<sheet.getRows();i++){     
    def MAC=sheet.getCell(0,i).getContents()
    CMMACList.add(MAC);
	}	 
workbook.close();
CMMACList.unique();     
def counter=0; 
context.setProperty("counter",counter) 
context.setProperty("CMMACList",CMMACList)

//groovy script in test case
import groovy.transform.*
import groovy.util.*
import com.eviware.soapui.model.testsuite.Assertable.AssertionStatus
def getCMMAC(){      
    synchronized(context.LoadTestContext){
            def CMMACList=context.LoadTestContext.CMMACList 
	       def counter=context.LoadTestContext.counter 
	       if (counter>=CMMACList.size()){counter=0}
	       String MAC=CMMACList[counter]
	       CMMACList.remove(MAC);		   
	       context.setProperty("counter",counter++);
	       context.setProperty("CMMACList",CMMACList);	       
	       return MAC;		
		}
}
def addCMMACtoList(String CMMAC){      	 
    synchronized(context.LoadTestContext){
            def CMMACList=context.LoadTestContext.CMMACList
            if(CMMACList==null){CMMACList=[]}
	       CMMACList.add(CMMAC)		 
	       context.setProperty("CMMACList",CMMACList)	
	   }	   	
}
def recordTimeThreadId(){
    Date date= new Date();	
    def threadId = Thread.currentThread().getId();     
    String recordTime=date.format("yyyy-MM-dd HH:mm:ss.SSSX ")	
    def record=(recordTime+" threadId:"+threadId+"");    
    return record;
}         
void generateLog(String CMMAC,File testLog){     
    def record=recordTimeThreadId()
    testStepSrc = testRunner.testCase.getTestStepByName("REST Request")
    Assertioncounter = testStepSrc.getAssertionList().size()
    for (AssertionCount in 0..Assertioncounter-1){
        def AssertionLog= ("CMMAC: "+CMMAC+" "+record+" Assertion :" +testStepSrc.getAssertionAt(AssertionCount).getName()
        + " :: " + testStepSrc.getAssertionAt(AssertionCount).getStatus())
        log.info(AssertionLog);//display the testing result information       
        testLog <<("${AssertionLog}\r\n") //input the log into the testing result report        
        error = testStepSrc.getAssertionAt(AssertionCount).getErrors()
        if (error != null)
           {
               def errorLog=("CMMAC: "+record+" "+CMMAC+" "+error[0].getMessage())	
               log.error(errorLog)//display the error log
               testLog <<("${errorLog}\r\n") //input the error log into the testing result report                  
            }
        }
        def endTime="CMMAC: "+CMMAC+" "+recordTimeThreadId()+"  end"+"********************"     
        testLog <<("${endTime}\r\n")      
 }
def runningCMMAC= getCMMAC(); 
File testLog= new File("E:/OSS/mThreadsTest.txt"); 
def beginTime="CMMAC: "+runningCMMAC+" "+recordTimeThreadId()+"  begin"+"********************"
testLog<<("${beginTime}\r\n")
testRunner.testCase.testSteps["REST Request"].setPropertyValue("CMMAC",runningCMMAC);//Run the CMMAC    
def testStep=testRunner.testCase.testSteps["REST Request"];
testStep.run(testRunner,context);
generateLog(runningCMMAC,testLog);//Generate test Log
addCMMACtoList(runningCMMAC);//Add the afterrunning CMMAC into restCMMAC list 
 