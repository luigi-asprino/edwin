package it.cnr.istc.stlab.edwin.endpoint;

//import org.apache.jena.fuseki.main.FusekiServer;
//import org.apache.jena.fuseki.main.FusekiServer.Builder;
//import org.apache.jena.query.ARQ;
//import org.apache.jena.query.Dataset;
//import org.apache.jena.query.DatasetFactory;
//import org.apache.jena.query.Query;
//import org.apache.jena.query.QueryExecutionFactory;
//import org.apache.jena.query.QueryFactory;
//import org.apache.jena.query.ResultSetFormatter;
//import org.apache.jena.rdf.model.Model;
//import org.apache.jena.sparql.engine.main.QC;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

public class Endpoint {

//	private static Logger logger = LoggerFactory.getLogger(Endpoint.class);
//	private Builder builder = FusekiServer.create();
//	public static final String DEFAULT_PATH = "/sparql.anything";
//	private String path;
//	private FusekiServer server;
//	private int port;
//	private static Endpoint instance;
//	public static final int DEFAULT_PORT = 3000;
//	private static final String PORT = "p", PATH = "e";
//
//	private static final String PREFIX = "http://example.org/";
//
//	private Endpoint() {
//		builder.port(DEFAULT_PORT);
//		path = DEFAULT_PATH;
//		port = DEFAULT_PORT;
//	}
//
//	public static Endpoint getInstance() {
//		if (instance == null) {
//			instance = new Endpoint();
//		}
//		return instance;
//	}
//
//	public void setPort(int port) {
//		this.port = port;
//		builder.port(port);
//	}
//
//	public void setPath(String path) {
//		if (path.charAt(0) != '/') {
//			this.path = "/" + path;
//		} else {
//			this.path = path;
//		}
//	}
//
//	public void start() {
//		Dataset ds = DatasetFactory.create();
//		QC.setFactory(ARQ.getContext(), EdwinOpExecutor.ExecutorFactory);
//		Model m = ds.getDefaultModel();
//
//		m.add(m.createResource(PREFIX + "s"), m.createProperty(PREFIX + "p"), m.createResource(PREFIX + "o"));
//		ds.commit();
//
//		logger.info("Starting sparql.anything endpoint..");
//		logger.info("The server will be listening on http://localhost:{}{}", port, path);
//		server = builder.add(path, ds).build();
//		server.start();
//	}
//
//	public void stop() {
//		server.stop();
//		logger.info("SPARQL Endpoint ended!");
//	}
//
//	public static void main(String[] args) {
////		Endpoint e = Endpoint.getInstance();
////		e.start();
//		QC.setFactory(ARQ.getContext(), EdwinOpExecutor.ExecutorFactory);
//
//		Dataset kb = DatasetFactory.createGeneral();
//		Model m = kb.getDefaultModel();
//
//		m.add(m.createResource(PREFIX + "s"), m.createProperty(PREFIX + "p"), m.createResource(PREFIX + "o"));
//
//		Query q = QueryFactory.create("SELECT * {?s ?p ?o}");
//		System.out.println(ResultSetFormatter.asText(QueryExecutionFactory.create(q, kb).execSelect()));
//	}

}
