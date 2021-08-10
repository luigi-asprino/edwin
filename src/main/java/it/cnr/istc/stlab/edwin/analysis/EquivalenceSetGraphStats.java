package it.cnr.istc.stlab.edwin.analysis;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

public class EquivalenceSetGraphStats {

	public long hetES = 0L;
	public long numberOfEquivalenceTriples = 0L, numberOfSpecializationTriples = 0L, oe = 0L, es = 0L, bns = 0L,
			es_with_bns = 0L, e = 0L, h_max = 0L, in = 0L, tl = 0L, tlWithoutBNs = 0L, oeInTL = 0L,
			oeInTLWithoutBNs = 0L, oe0 = 0L, oe0_bns = 0L, es0 = 0L, es0bns = 0L, tl0 = 0, tl0WithoutBNs = 0L,
			oeInTL0 = 0, oeInTl0WithoutBN = 0;
	public Set<String> equivalencePropertiesUsed = new HashSet<>(), specializationPropertiesUsed = new HashSet<>();
	public Map<Long, Long> heightDistribution = new HashMap<>();
	public Map<Long, Long> indirectExtensionalSizeDistribution = new HashMap<>();
	public Map<Long, Long> iesPerThreshold = new HashMap<>();
	private final String neq_label = "Number of Equivalence Triples", nsub_label = "Number of Specialization Triples",
			eqp_label = "Equivalence Properties Used", subp_label = "Specialization Properties Used",
			neqp_label = "Number of Equivalence Properties Used",
			nsubp_label = "Number of Specialization Properties Used", es_label = "Number of Equivalence Sets",
			oe_label = "Number of Observed Entities", oe_bn_label = "Number of Observed Entities without BNs",
			r_label = "Ratio between ES and OE", bns_label = "Number of Blank Nodes",
			r_bn_label = "Ratio between ES and OE without blank nodes", edges_label = "Number of Edges",
			es_bn = "Number of Equivalence Sets without BNs", h_max_label = "Max Height",
			heightDistribution_label = "Height Distribution", in_label = "Number of Isolated Equivalence Sets",
			hd_label = "hd", tl_label = "Top Level Equivalence Sets",
			tlWithoutBNs_label = "Top Level Equivalence Sets Without Blank Nodes",
			oeInTL_label = "Number of Observed Entities in Top Level Equivalence Sets",
			oeInTLWithoutBNs_label = "Number of Observed Entities Without Blank Nodes in Top Level Equivalence Sets",
			rtl_label = "Ratio between Number of Top Level ESs and Observed Entities in Top Level ESs",
			rtlwithoutBN_label = "Ratio between Number of Top Level ESs and Observed Entities in Top Level ESs without BNs",
			oe0_label = "Number of Observed Entities with Empty Extesion",
			oe0bns_label = "Number of Observed Entities with Empty Extesion Excluding Blank Nodes",
			es0_label = "Number of Empty Equivalence Sets",
			es0bns_label = "Number of Empty Equivalence Sets Without Blank Nodes",
			iesDistribution_label = "Distribution of the Indirect Extensional Size of the Equivalence Sets",
			tl0_label = "Top Level Equivalence Sets With Empty Extension",
			tl0bns_label = "Top Level Equivalence Sets With Empty Extension Without Blank Nodes",
			oeInTl0_label = "Number of Observed Entities in TL0",
			tl0WithoutBNs_label = "Number of Observed Entities Without BNs in TL0", ies_n = "IES(n)",
			density = "Density", hetES_label = "Heterogeneous Equivalence Sets";

	public EquivalenceSetGraphStats() {

	}

	public String getTextualFileFormat() {

		return getJSONObject().toString();

	}

	JSONObject getJSONObject() {

		JSONObject stats = new JSONObject();
		stats.put(neq_label, numberOfEquivalenceTriples);
		stats.put(nsub_label, numberOfSpecializationTriples);
		stats.put(neqp_label, equivalencePropertiesUsed.size());
		stats.put(eqp_label, equivalencePropertiesUsed);
		stats.put(nsubp_label, specializationPropertiesUsed.size());
		stats.put(subp_label, specializationPropertiesUsed);
		stats.put(oe_label, oe);
		stats.put(oe_bn_label, (oe - bns));
		stats.put(bns_label, bns);
		stats.put(es_label, es);
		stats.put(hetES_label, hetES);
		stats.put(es_bn, (es - es_with_bns));
		stats.put(r_label, transformDouble((double) ((double) es / (double) oe)));
		stats.put(r_bn_label, transformDouble((double) ((double) (es - es_with_bns) / (double) (oe - bns))));
		stats.put(edges_label, e);
		if (e > 0)
			stats.put(density, ((double) e / ((double) (es * (es - 1)))));
		stats.put(h_max_label, h_max);
		stats.put(heightDistribution_label, heightDistribution);
		// hd
		if (heightDistribution.size() > 0) {
			long max = Collections.max(heightDistribution.keySet());
			JSONArray hd = new JSONArray();
			for (long i = 0; i <= max; i++) {
				JSONObject point = new JSONObject();
				if (heightDistribution.containsKey(i)) {
					point.put("x", i);
					point.put("y", ((double) heightDistribution.get(i) / (double) es));
					hd.put(point);
				}
			}
			stats.put(hd_label, hd);
		}
		stats.put(in_label, in);
		stats.put(tl_label, tl);
		stats.put(tlWithoutBNs_label, tlWithoutBNs);
		stats.put(oeInTL_label, oeInTL);
		stats.put(oeInTLWithoutBNs_label, oeInTLWithoutBNs);
		stats.put(rtl_label, transformDouble((double) ((double) tl / (double) oeInTL)));
		stats.put(rtlwithoutBN_label, transformDouble((double) ((double) tlWithoutBNs / (double) oeInTLWithoutBNs)));
		stats.put(oe0_label, oe0);
		stats.put(oe0bns_label, oe0_bns);
		stats.put(es0_label, es0);
		stats.put(es0bns_label, es0bns);
		stats.put(iesDistribution_label, indirectExtensionalSizeDistribution);
		stats.put(ies_n, iesPerThreshold);
		stats.put(tl0_label, tl0);
		stats.put(tl0bns_label, tl0WithoutBNs);
		stats.put(oeInTl0_label, oeInTL0);
		stats.put(tl0WithoutBNs_label, oeInTl0WithoutBN);

		return stats;

	}

	public void toTSVFile(String path) throws IOException {
		FileOutputStream fos = new FileOutputStream(new File(path));

		fos.write(String.format("%s\t%d\n", neq_label, numberOfEquivalenceTriples).getBytes());
		fos.write(String.format("%s\t%d\n", nsub_label, numberOfSpecializationTriples).getBytes());
		fos.write(String.format("%s\t%d\n", neqp_label, equivalencePropertiesUsed.size()).getBytes());
		fos.write(String.format("%s\t%s\n", eqp_label, equivalencePropertiesUsed.toString()).getBytes());
		fos.write(String.format("%s\t%d\n", nsubp_label, specializationPropertiesUsed.size()).getBytes());
		fos.write(String.format("%s\t%s\n", subp_label, specializationPropertiesUsed.toString()).getBytes());
		fos.write(String.format("%s\t%d\n", oe_label, oe).getBytes());
		fos.write(String.format("%s\t%d\n", oe_bn_label, (oe - bns)).getBytes());
		fos.write(String.format("%s\t%d\n", bns_label, bns).getBytes());
		fos.write(String.format("%s\t%d\n", es_label, es).getBytes());
		fos.write(String.format("%s\t%d\n", es_bn, (es - es_with_bns)).getBytes());
		fos.write(String.format("%s\t%d\n", r_label, transformDouble((double) ((double) es / (double) oe))).getBytes());
		fos.write(String.format("%s\t%d\n", r_bn_label,
				transformDouble((double) ((double) (es - es_with_bns) / (double) (oe - bns)))).getBytes());
		fos.write(String.format("%s\t%d\n", edges_label, e).getBytes());
		fos.write(String.format("%s\t%d\n", h_max_label, h_max).getBytes());
		fos.write(String.format("%s\t%d\n", in_label, in).getBytes());
		fos.write(String.format("%s\t%d\n", tl_label, tl).getBytes());
		fos.write(String.format("%s\t%d\n", tlWithoutBNs_label, tlWithoutBNs).getBytes());
		fos.write(String.format("%s\t%d\n", oeInTL_label, oeInTL).getBytes());
		fos.write(String.format("%s\t%d\n", oeInTLWithoutBNs_label, oeInTLWithoutBNs).getBytes());
		fos.write(String.format("%s\t%d\n", rtl_label, transformDouble((double) ((double) tl / (double) oeInTL)))
				.getBytes());
		fos.write(String.format("%s\t%d\n", rtlwithoutBN_label,
				transformDouble((double) ((double) tlWithoutBNs / (double) oeInTLWithoutBNs))).getBytes());
		fos.write(String.format("%s\t%d\n", oe0_label, oe0).getBytes());
		fos.write(String.format("%s\t%d\n", oe0bns_label, oe0_bns).getBytes());
		fos.write(String.format("%s\t%d\n", es0_label, es0).getBytes());
		fos.write(String.format("%s\t%d\n", es0bns_label, es0bns).getBytes());
		fos.write(String.format("%s\t%d\n", "IES(1)", (es - es0)).getBytes());
		fos.write(String.format("%s\t%d\n", "IES(10)", iesPerThreshold.get(10L)).getBytes());
		fos.write(String.format("%s\t%d\n", "IES(100)", iesPerThreshold.get(100L)).getBytes());
		fos.write(String.format("%s\t%d\n", "IES(1K)", iesPerThreshold.get(1000L)).getBytes());
		fos.write(String.format("%s\t%d\n", "IES(1M)", iesPerThreshold.get(1000000L)).getBytes());
		fos.write(String.format("%s\t%d\n", "IES(1B)", iesPerThreshold.get(1000000000L)).getBytes());
		fos.write(String.format("%s\t%d\n", oeInTl0_label, oeInTL0).getBytes());
		fos.write(String.format("%s\t%d\n", tl0WithoutBNs_label, oeInTl0WithoutBN).getBytes());
		fos.write(String.format("%s\t%d\n", tl0_label, tl0).getBytes());
		fos.write(String.format("%s\t%d\n", tl0bns_label, tl0WithoutBNs).getBytes());
		fos.write(String.format("%s\t%d\n", hetES_label, hetES).getBytes());

		fos.flush();
		fos.close();
	}

	private int transformDouble(double d) {
		return (int) (d * 1000);
	}

}
