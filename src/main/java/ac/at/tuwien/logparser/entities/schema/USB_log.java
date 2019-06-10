/* CVS $Id: $ */
package ac.at.tuwien.logparser.entities.schema;

import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

public class USB_log {
    /** <p>The RDF model that holds the vocabulary terms</p> */
    private static final OntModel M_MODEL = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM, null );


    /** <p>The namespace of the vocabulary as a string</p> */
    public static final String NS = "http://sepses.ifs.tuwien.ac.at/vocab/usbLog#";
    
    /** <p>The namespace of the vocabulary as a string</p>
     * @return namespace as String
     * @see #NS */
    public static String getURI() {return NS;}
    
    /** <p>The namespace of the vocabulary as a resource</p> */
    public static final Resource NAMESPACE = M_MODEL.createResource( NS );
    
    public static final OntClass UsbLog = M_MODEL.createClass( "http://sepses.ifs.tuwien.ac.at/vocab/usbLog#USBDeviceLogEntry" );

    public static final DatatypeProperty id = M_MODEL.createDatatypeProperty( "http://sepses.ifs.tuwien.ac.at/vocab/usbLog#id" );

    public static final DatatypeProperty timestamp = M_MODEL.createDatatypeProperty( "http://sepses.ifs.tuwien.ac.at/vocab/usbLog#timestamp" );

    public static final DatatypeProperty instaceId = M_MODEL.createDatatypeProperty( "http://sepses.ifs.tuwien.ac.at/vocab/usbLog#instanceId" );

    public static final DatatypeProperty logMessage = M_MODEL.createDatatypeProperty( "http://sepses.ifs.tuwien.ac.at/vocab/usbLog#logMessage" );

}
