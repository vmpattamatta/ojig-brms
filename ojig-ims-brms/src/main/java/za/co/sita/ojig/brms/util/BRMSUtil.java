package za.co.sita.ojig.brms.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.drools.KnowledgeBase;
import org.drools.SystemEventListenerFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.jbpm.task.service.TaskClient;
import org.jbpm.task.service.hornetq.CommandBasedHornetQWSHumanTaskHandler;
import org.jbpm.task.service.hornetq.HornetQTaskClientConnector;
import org.jbpm.task.service.hornetq.HornetQTaskClientHandler;

import za.co.sita.ojig.ims.hib.bean.Incident;

public class BRMSUtil {
	public static final String groupId = "za.co.sita";
	public static final String artifactId = "ojig-ims-brms";
	public static final String version = "0.0.1-SNAPSHOT";
	protected StatefulKnowledgeSession session;
	protected TaskClient client;

	public enum Strategy {
		SINGLETON, REQUEST, PROCESS_INSTANCE;
	}

	public BRMSUtil() throws Exception {
		initSession();
	}

	public void initSession() throws Exception {
		if (session == null) {
			Map<String, ResourceType> res = new HashMap<String, ResourceType>();
			res.put("ims-process-flow.bpmn", ResourceType.BPMN2);
			res.put("ims-rule-list.drl", ResourceType.DRL);
			KnowledgeBase base = createKnowledgeBase(res);
			session = base.newStatefulKnowledgeSession();
			session.getWorkItemManager().registerWorkItemHandler("Human Task", new CommandBasedHornetQWSHumanTaskHandler(session));
			client = new TaskClient(new HornetQTaskClientConnector("HornetQConnector" + UUID.randomUUID(), new HornetQTaskClientHandler(SystemEventListenerFactory.getSystemEventListener())));
			client.connect("127.0.0.1", 5446);
			CommandBasedHornetQWSHumanTaskHandler handler = new CommandBasedHornetQWSHumanTaskHandler(session);
			handler.setClient(client);
			handler.connect();
			session.getWorkItemManager().registerWorkItemHandler("Human Task", handler);
		}
	}

	public List<Incident> getIncidentsForApproval() {
		List<Incident> ret = new ArrayList<Incident>();
		return ret;
	}

	public List<Incident> getIncidentsForAssigning() {
		List<Incident> ret = new ArrayList<Incident>();
		return ret;
	}

	public List<Incident> getIncidentsForCompletion() {
		List<Incident> ret = new ArrayList<Incident>();
		return ret;
	}

	protected KnowledgeBase createKnowledgeBase(Map<String, ResourceType> resources) throws Exception {
		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
		for (Map.Entry<String, ResourceType> entry : resources.entrySet()) {
			kbuilder.add(ResourceFactory.newClassPathResource(entry.getKey()), entry.getValue());
		}
		return kbuilder.newKnowledgeBase();
	}

}
