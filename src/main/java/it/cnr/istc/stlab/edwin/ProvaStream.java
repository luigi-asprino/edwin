package it.cnr.istc.stlab.edwin;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;

import org.eclipse.rdf4j.rio.UnsupportedRDFormatException;

public class ProvaStream {
	public static void main(String[] args) throws FileNotFoundException, UnsupportedRDFormatException, URISyntaxException {
//		FileOutputStream fos = new FileOutputStream(new File("/Users/lgu/Desktop/ab.ttl"));
//		StreamRDF s = StreamRDFLib.writer(fos);
//		Model m = ModelFactory.createDefaultModel();
//		s.triple(new Triple(NodeFactory.createURI("a"), m.createResource("b").asNode(),
//				m.createResource("c").asNode()));
//		s.finish();
		
		System.out.println(RocksDBBackedEquivalenceSetGraph.checkIRI("a"));

//		RDFWriter rw = Rio.createWriter(RDFFormat.NTRIPLES, fos, "http://acioa.com/");
//		rw.startRDF();
//		ValueFactory factory = SimpleValueFactory.getInstance();
//		
//		rw.handleStatement(
//				factory.createStatement(factory.createIRI("a"), factory.createIRI("b"), factory.createIRI("c")));
////		rw.handleStatement(
////				factory.createStatement(factory.createIRI("http://a.com/a"), factory.createIRI("http://a.com/b"), factory.createIRI("http://a.com/c")));
//////		rw.handleStatement(
//////				factory.createStatement(factory.createIRI("http://a.com/a"), factory.createIRI("http://a.com/b"), factory.createli));
//		rw.endRDF();
	}
}
