package it.cnr.istc.stlab.edwin.endpoint;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpTriple;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.main.OpExecutor;
import org.apache.jena.sparql.engine.main.OpExecutorFactory;

public class EdwinOpExecutor extends OpExecutor {

	public final static OpExecutorFactory ExecutorFactory = new OpExecutorFactory() {
		@Override
		public OpExecutor create(ExecutionContext execCxt) {
			return new EdwinOpExecutor(execCxt);
		}
	};

//	private static final Logger logger = LoggerFactory.getLogger(EdwinOpExecutor.class);

	public EdwinOpExecutor(ExecutionContext execCxt) {
		super(execCxt);
	}

	protected QueryIterator execute(OpBGP opBGP, QueryIterator input) {
		System.out.println("hey");

		opBGP.getPattern().iterator().forEachRemaining(t -> {
			System.out.println(t.getObject().toString());
			System.out.println(t.getSubject().toString());
			System.out.println(t.getPredicate().toString());
		});

		System.out.println();

		QueryIterator qIter = super.execute(opBGP, input);

		qIter.forEachRemaining(b -> {
			b.vars().forEachRemaining(v -> {
				Node n = b.get(v);
				System.out.println(v.getName() + " -> " + n.toString());
			});
		});

		return super.execute(opBGP, input);
	}

	protected QueryIterator execute(OpTriple opTriple, QueryIterator input) {

		System.out.println("hey");
		if (opTriple.getTriple().getPredicate().isURI()) {
			System.out.println("Hey " + opTriple.getTriple().getPredicate().getURI());
		}

		return super.execute(opTriple.asBGP(), input);
	}

}
