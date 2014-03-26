package za.co.sita.ojig.brms.test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.drools.KnowledgeBase;
import org.drools.SystemEventListenerFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderError;
import org.drools.builder.KnowledgeBuilderErrors;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.ProcessInstance;
import org.jbpm.task.query.TaskSummary;
import org.jbpm.task.service.TaskClient;
import org.jbpm.task.service.hornetq.CommandBasedHornetQWSHumanTaskHandler;
import org.jbpm.task.service.hornetq.HornetQTaskClientConnector;
import org.jbpm.task.service.hornetq.HornetQTaskClientHandler;
import org.jbpm.task.service.responsehandlers.BlockingTaskSummaryResponseHandler;
import org.jbpm.test.JbpmJUnitTestCase;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import za.co.sita.ojig.brms.util.DBUtil;
import za.co.sita.ojig.ims.hib.bean.Incident;

@SuppressWarnings({ "resource" })
public class BRMSUnitTest extends JbpmJUnitTestCase {
	public void testProcess() throws Exception {
		ApplicationContext context = new ClassPathXmlApplicationContext("brmsContext.xml");
		DBUtil util = (DBUtil) context.getBean("DBUtil");
		Map<String, Object> instanceVar = new HashMap<String, Object>();
		Incident incident = new Incident(util.getStatusFromId(1), -26.005814, 28.117176, "cnr bekker le roux avenue midrand", "/home/main/img.jpg", new Date(), "", 0d, null);
		incident.setId(1l);
		instanceVar.put("util", util);
		instanceVar.put("incident", incident);
		Map<String, ResourceType> res = new HashMap<String, ResourceType>();
		res.put("ims-process-flow.bpmn", ResourceType.BPMN2);
		res.put("ims-rule-list.drl", ResourceType.DRL);
		KnowledgeBase kb = createKnowledgeBase(res);
		StatefulKnowledgeSession ksession = kb.newStatefulKnowledgeSession();
		ksession.setGlobal("util", util);
		ksession.setGlobal("incident", incident);
		ProcessInstance processInstance = ksession.startProcess("za.co.sita.ojig.brms.IMSProcess", instanceVar);
		ksession.fireAllRules();
		System.out.println(processInstance.getClass() + " :: " + processInstance.getId() + " :: " + processInstance.getProcessId() + " :: " + processInstance.getState());
		TaskClient client = new TaskClient(new HornetQTaskClientConnector("HornetQConnector" + UUID.randomUUID(), new HornetQTaskClientHandler(SystemEventListenerFactory.getSystemEventListener())));
		client.connect("127.0.0.1", 5446);
		CommandBasedHornetQWSHumanTaskHandler handler = new CommandBasedHornetQWSHumanTaskHandler(ksession);
		handler.setClient(client);
		handler.connect();
		ksession.getWorkItemManager().registerWorkItemHandler("Human Task", handler);
		BlockingTaskSummaryResponseHandler resHandler = new BlockingTaskSummaryResponseHandler();
		client.getTasksOwned("admin", "en-UK", resHandler);
		for (TaskSummary summary : resHandler.getResults()) {
			System.out.println(summary.getId() + " :: " + summary.getName() + " :: " + summary.getProcessId());
		}
	}

	@Test
	public void validateRuleFile() {
		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
		kbuilder.add(ResourceFactory.newClassPathResource("ims-rule-list.drl"), org.drools.builder.ResourceType.DRL);
		KnowledgeBuilderErrors errors = kbuilder.getErrors();
		if (errors.size() > 0) {
			for (KnowledgeBuilderError error : errors) {
				System.err.println(error);
			}
		}
	}

	@Test
	public void testRuleFile() {
		ApplicationContext context = new ClassPathXmlApplicationContext("brmsContext.xml");
		DBUtil util = (DBUtil) context.getBean("DBUtil");
		Incident incident = new Incident(util.getStatusFromId(1), -26.005814, 28.117176, "cnr bekker le roux avenue midrand", "/home/main/img.jpg", new Date(), "", 0d, null);
		incident.setId(1l);
		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
		kbuilder.add(ResourceFactory.newClassPathResource("ims-rule-list.drl"), org.drools.builder.ResourceType.DRL);
		KnowledgeBuilderErrors errors = kbuilder.getErrors();
		if (errors.size() > 0) {
			for (KnowledgeBuilderError error : errors) {
				System.err.println(error);
			}
		} else {
			StatefulKnowledgeSession session = kbuilder.newKnowledgeBase().newStatefulKnowledgeSession();
			session.setGlobal("util", util);
			session.setGlobal("incident", incident);
			session.fireAllRules();
		}
	}
}
