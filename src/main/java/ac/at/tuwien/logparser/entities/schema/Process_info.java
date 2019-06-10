/* CVS $Id: $ */
package ac.at.tuwien.logparser.entities.schema;

import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Vocabulary definitions from file:/Users/Agnes/Documents/TU/Diplomarbeit/sba-file-system-log-service/src/main/resources/vocabs/process-info.rdfs 
 * @author Auto-generated by schemagen on 30 Nov 2018 11:36 
 */
public class Process_info {
    /** <p>The RDF model that holds the vocabulary terms</p> */
    private static final OntModel M_MODEL = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM, null );


    /** <p>The namespace of the vocabulary as a string</p> */
    public static final String NS = "http://sepses.ifs.tuwien.ac.at/vocab/processInfo#";
    
    /** <p>The namespace of the vocabulary as a string</p>
     * @return namespace as String
     * @see #NS */
    public static String getURI() {return NS;}
    
    /** <p>The namespace of the vocabulary as a resource</p> */
    public static final Resource NAMESPACE = M_MODEL.createResource( NS );
    
    public static final OntClass ProcessInfo = M_MODEL.createClass( "http://sepses.ifs.tuwien.ac.at/vocab/processInfo#ProcessInfo" );

    public static final DatatypeProperty id = M_MODEL.createDatatypeProperty( "http://sepses.ifs.tuwien.ac.at/vocab/processInfo#id" );

    public static final DatatypeProperty operation = M_MODEL.createDatatypeProperty( "http://sepses.ifs.tuwien.ac.at/vocab/processInfo#operation" );

    public static final DatatypeProperty timestamp = M_MODEL.createDatatypeProperty( "http://sepses.ifs.tuwien.ac.at/vocab/processInfo#timestamp" );

    public static final DatatypeProperty processName = M_MODEL.createDatatypeProperty( "http://sepses.ifs.tuwien.ac.at/vocab/processInfo#processName" );

    public static final DatatypeProperty pid = M_MODEL.createDatatypeProperty( "http://sepses.ifs.tuwien.ac.at/vocab/processInfo#pid" );

}
