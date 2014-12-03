/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pipinan.githubcrawler;

import java.util.concurrent.TimeUnit;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.neo4j.graphdb.schema.Schema;

/**
 *
 * @author Administrator
 */
public class Neo4jGraph {

    private static void registerShutdownHook(final GraphDatabaseService graphDb) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                graphDb.shutdown();
            }
        });
    }

    private static enum RelTypes implements RelationshipType {

        FOLLOWS
    }

    private GraphDatabaseService graphDb;
    private static final String DB_PATH = "target/github-login-db";

    public Neo4jGraph() {
        graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(DB_PATH);
        {
            IndexDefinition indexDefinition;
            try (Transaction tx = graphDb.beginTx()) {
                Schema schema = graphDb.schema();
                indexDefinition = schema.indexFor(DynamicLabel.label("User")).on("login").create();
                tx.success();
            }

            try (Transaction tx = graphDb.beginTx()) {
                Schema schema = graphDb.schema();
                schema.awaitIndexOnline(indexDefinition, 10, TimeUnit.SECONDS);
            }
        }
        registerShutdownHook(graphDb);
    }

    public void addUser(String login) {
        try (Transaction tx = graphDb.beginTx()) {
            Label label = DynamicLabel.label("User");
            Node userNode = graphDb.createNode(label);
            userNode.setProperty("login", login);
            tx.success();
        }
    }

    public Node findAnUser(String login) {
        Label label = DynamicLabel.label("User");
        try (Transaction tx = graphDb.beginTx();
                ResourceIterator<Node> users = graphDb.findNodesByLabelAndProperty(label, "login", login).iterator()) {
            if (users.hasNext()) {
                return users.next();
            }
        }
        return null;
    }
    
    public void createFollowRelationship(Node source, Node destination){
        try(Transaction tx = graphDb.beginTx()){
            source.createRelationshipTo(destination, RelTypes.FOLLOWS);
            tx.success();
        }
    }
}
