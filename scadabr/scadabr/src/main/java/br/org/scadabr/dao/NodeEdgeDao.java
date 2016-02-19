/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.scadabr.dao;

import br.org.scadabr.vo.Edge;
import br.org.scadabr.vo.EdgeIterator;
import br.org.scadabr.vo.EdgeType;
import br.org.scadabr.vo.NodeIterator;
import br.org.scadabr.vo.NodeType;
import br.org.scadabr.vo.VO;
import javax.inject.Named;

/**
 *
 * @author aploese
 * @param <T>
 */
@Named
public interface NodeEdgeDao<T extends VO<T>> {
    
    <U extends VO<U>> U saveNode(U node);
    
    T getNodeById(int id);
    
    boolean isNodeExisting(int id);
    
    boolean deleteNode(int id);
    
    default boolean deleteNode(VO<?> node) {
        return deleteNode(node.getId());
    }

    void iterateNodes(NodeIterator nodeIterator);
    
    long countNodes();

    long countNodes(NodeType nodeType);

    default Edge saveEdge(Edge edge) {
        saveEdge(edge.getSrcNodeId(), edge.getDestNodeId(), edge.getEdgeType());
        return edge;
    }
    
    void saveEdge(int srcNodeId, int destNodeId, EdgeType edgeType);

    default boolean isEdgeExisting(Edge edge) {
        return isEdgeExisting(edge.getSrcNodeId(), edge.getDestNodeId(), edge.getEdgeType());
    }
    
    boolean isEdgeExisting(int srcNodeId, int destNodeId, EdgeType edgeType);
    
    boolean deleteEdge(int srcNodeId, int destNodeId, EdgeType edgeType);

    default boolean deleteEdge(Edge edge) {
        return deleteEdge(edge.getSrcNodeId(), edge.getDestNodeId(), edge.getEdgeType());
    }

    void iterateEdges(final EdgeIterator edgeIterator); 

    long countEdges();

    long countEdges(EdgeType edgeType);
    
    void wipeDB();

}
