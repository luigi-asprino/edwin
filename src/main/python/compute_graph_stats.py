'''
Created on Apr 8, 2019

@author: lgu
'''
import snap
import codecs
import logging
import sys
import json

logging.basicConfig(format='%(asctime)s : %(levelname)s : %(message)s', level=logging.INFO)
logger = logging.getLogger(__name__)
progress_cnt = 10000


def loadGraph(folder):
    
    edge_list = folder + "/edgelist.tsv"
    node_list = folder + "/nodelist.tsv"
    
    G = snap.TNGraph.New()
    with codecs.open(node_list, 'r', 'utf-8') as dump_file:
        line = dump_file.readline()
        current_line = 0
        while line:
            if (current_line > 0 and current_line % progress_cnt == 0):
                    logger.info("Nodes {}".format(current_line))
            current_line += 1
            G.AddNode(int(line))
            line = dump_file.readline()
    
    with codecs.open(edge_list, 'r', 'utf-8') as dump_file:
        line = dump_file.readline()
        current_line = 0
        while line:
            if (current_line > 0 and current_line % progress_cnt == 0):
                    logger.info("Edges {}".format(current_line))
            current_line += 1
            l = line.split('\t')
            G.AddEdge(int(l[0]), int(l[1]))
            line = dump_file.readline()
    
    return G


def computeNumberOfWeaklyConnectedComponentsPerSize(graph, outFile, fill):
    logger.info("Computing Number of Weakly Connected Components Per Component Size")
    fw_cc = open(outFile, 'w')
    CntV = snap.TIntPrV()
    snap.GetWccSzCnt(graph, CntV)
    fw_cc.write('Size of WCC\tNumber of WCCs\n')
    last = 0
    array = []
    for p in CntV:
        if(fill & p.GetVal1() - last > 1):
            for i in range(last + 1, p.GetVal1()):
                fw_cc.write(str(i) + '\t' + str(0) + '\n')
        fw_cc.write(str(p.GetVal1()) + '\t' + str(p.GetVal2()) + '\n')
        array.append({"x":p.GetVal1(), "y":p.GetVal2()})
        last = p.GetVal1()
    with open(outFile + '.json', 'w') as file:
        json.dump(array, file)
    logger.info("Number of Weakly Connected Components Per Component Size!")
    logger.info("Number of Weakly Connected Components Per Component Size Exported to " + outFile)

        
def computeWeaklyConnectedComponents(graph, outFile):
    logger.info("Computing Weakly Connected Components")
    fw_cc = open(outFile, 'w')
    Components = snap.TCnComV()
    snap.GetWccs(graph, Components)
    for CnCom in Components:
        for item in CnCom:
            fw_cc.write(str(item) + "\n")
        fw_cc.write("\n")
    logger.info("Weakly Connected Components Computed!")
    logger.info("Weakly Connected Components Exported to " + outFile)

            
def computeNumberOfStronglyConnectedComponentsPerSize(graph, outFile, fill):
    logger.info("Computing Strongly Connected Components Per Component Size")
    fw_cc = open(outFile, 'w')
    CntV = snap.TIntPrV()
    fw_cc.write('Size of SCC\tNumber of SCCs\n')
    snap.GetSccSzCnt(graph, CntV)
    last = 0
    array = []
    for p in CntV:
        if(fill & p.GetVal1() - last > 1):
            for i in range(last + 1, p.GetVal1()):
                fw_cc.write(str(i) + '\t' + str(0) + '\n')
        fw_cc.write(str(p.GetVal1()) + '\t' + str(p.GetVal2()) + '\n')
        array.append({"x":p.GetVal1(), "y":p.GetVal2()})
        last = p.GetVal1()
    
    with open(outFile + '.json', 'w') as file:
        json.dump(array, file)
        
    logger.info("Number of Strongly Connected Components Per Component Size!")
    logger.info("Number of Strongly Connected Components Per Component Size Exported to " + outFile)

    
def computeStronglyConnectedComponents(graph, outFile):
    logger.info("Computing Strongly Connected Components")
    fw_cc = open(outFile, 'w')
    Components = snap.TCnComV()
    snap.GetSccs(graph, Components)
    for CnCom in Components:
        for item in CnCom:
            fw_cc.write(str(item) + "\n")
        fw_cc.write("\n")
    logger.info("Strongly Connected Components Computed!")
    logger.info("Strongly Connected Components Exported to " + outFile)

        
if __name__ == '__main__':
    
    esg_folder = sys.argv[1]
    stats_foder = sys.argv[1]
    if len(sys.argv) > 2:
        stats_foder = sys.argv[2]
        
    graph = loadGraph(esg_folder)
    computeStronglyConnectedComponents(graph, stats_foder + "/strongly_connected_components")
    computeNumberOfStronglyConnectedComponentsPerSize(graph, stats_foder + "/number_of_strongly_connected_components_per_size.tsv", False)
    computeWeaklyConnectedComponents(graph, stats_foder + "/weakly_connected_components")
    computeNumberOfWeaklyConnectedComponentsPerSize(graph, stats_foder + "/number_of_weakly_connected_components_per_size.tsv", False)
   
