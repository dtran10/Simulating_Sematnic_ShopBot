import java.util.HashSet;
import java.util.Vector;


import javax.swing.JFileChooser;
import javax.swing.*;
// my own addition

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NsIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.ReasonerRegistry;
import com.hp.hpl.jena.sparql.core.ResultBinding;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.RDF;


public class MyShopBot {
	
	// file selector
	JFileChooser fc = new JFileChooser();
	

	// change the paths for the following files
	private static String ontologyURL = "C:/Users/Home/workspace/Testing/src/camera.owl";
	private static String ontologyNS = "http://www.liyangyu.com/camera#";
	private static String requestRdf = "C:/Users/Home/workspace/Testing/src/myCameraDescription.rdf";
	private static String catalog = "C:/Users/Home/workspace/Testing/src/catalogExample1.rdf";
	private static final int MIN = 0;
	private static final int MAX = 1;
	
	private String targetItem = null;
	private String targetType = null;
	private Vector candidateItems = null;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
	
		MyShopBot myShopBot = new MyShopBot();
		myShopBot.work();
		
	}
	
	// constructor used to set the fields using a file chooser
	public MyShopBot() {
		
		int option = JOptionPane.showConfirmDialog(null, "Enter your own FILES or use DEFAULT?");
		
		// choose your own owl and rdf files
		if (option == JOptionPane.YES_OPTION) {
			
			System.out.println("CHOOSE A OWL FILE FOR YOUR ONTOLOGY");
			if (fc.showOpenDialog(null) == fc.APPROVE_OPTION) {
				ontologyURL = fc.getSelectedFile().getAbsolutePath();
			}
			String nsInput = JOptionPane.showInputDialog(null, "Enter your namespace", "http://www.liyangyu.com/camera#");
			if (nsInput != null || nsInput != "") {
				ontologyNS = nsInput;
			}
			System.out.println("CHOOSE A RDF FILE FOR CAMERA INFORMATION");
			if (fc.showOpenDialog(null) == fc.APPROVE_OPTION) {
				requestRdf = fc.getSelectedFile().getAbsolutePath();
			}
			System.out.println("CHOOSE A RDF FILE FOR YOUR CATALOG");
			if (fc.showOpenDialog(null) == fc.APPROVE_OPTION) {
				catalog = fc.getSelectedFile().getAbsolutePath();
			}
		}
		
		// sets the default values to match the path for your own computer and files
		else if (option == JOptionPane.NO_OPTION) {
			ontologyURL = "C:/Users/Home/workspace/Testing/src/camera.owl";
			ontologyNS = "http://www.liyangyu.com/camera#";
			requestRdf = "C:/Users/Home/workspace/Testing/src/myCameraDescription.rdf";
			catalog = "C:/Users/Home/workspace/Testing/src/catalogExample1.rdf";
		}
		
		else {
			System.out.println("Terminating ShopBot");
			System.exit(1);
			
		}
		
	}
	
	private void work() {
		
		// for the product we are looking for, get its URI and type info
		Model requestModel = getModel(requestRdf);
		if ( getItemToSearch(requestModel) == false ) {
			System.out.println("your request description is not complete!");
		}
		System.out.println("this URI describes the resource you are looking for:");
		System.out.println("<" + targetItem + ">");
		System.out.println("its type is given by the following class:");
		System.out.println("<" + targetType + ">");
		
		// find all the requested parameters
		
		CameraDescription myCamera = new CameraDescription();
		
		// gets the pixels from the rdf file and sets it for myCamera
		String targetPixel = getPixel(requestModel,targetItem);
		myCamera.setPixel(targetPixel);
		show("pixel(target)",targetPixel);
		
		// gets the target focal length from the file and sets it for myCamera
		String targetFocalLength = getFocalLength(requestModel,targetItem);
		myCamera.setFocalLength(targetFocalLength);
		show("focalLength(target)",targetFocalLength);
		
		// gets the min target Aperture and sets it for myCamera
		String targetAperture = getAperture(requestModel,targetItem,MyShopBot.MIN);
		myCamera.setMinAperture(targetAperture);
		show("min aperture(target)",targetAperture);
		
		// gets the max target Aperture and sets it for myCamera
		targetAperture = getAperture(requestModel,targetItem,MyShopBot.MAX);
		myCamera.setMaxAperture(targetAperture);
		show("max aperture(target)",targetAperture);
		
		// gets the min shutter speed and sets it for myCamera
		String targetShutterSpeed = getShutterSpeed(requestModel,targetItem,MyShopBot.MIN);
		myCamera.setMinShutterSpeed(targetShutterSpeed);
		show("min shutterSpeed(target)",targetShutterSpeed);
		
		// gets the max shutter speed and sets it for myCamera
		targetShutterSpeed = getShutterSpeed(requestModel,targetItem,MyShopBot.MAX);
		myCamera.setMaxShutterSpeed(targetShutterSpeed);
		show("max shutterSpeed(target)",targetShutterSpeed);
		
		// create a another camera object in order to compare it to the one we want
		CameraDescription currentCamera = new CameraDescription();
		
		while ( true ) {

			// creates a model from reading in the catalog rdf file
			Model catalogModel = getModel(catalog);
				
			// see if it has potential candidates
			if ( isCandidate(catalogModel) == false ) {
				continue;
			}
			
			// create ontology model for inferencing
			OntModel ontModel =	ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RULE_INF,catalogModel);
			FileManager.get().readModel(ontModel,ontologyURL);
			
			// which item could be it?
			candidateItems = findCandidateItem(ontModel); // finds potential candidate cameras contained in the catalog.
			
			// if there's no candidates, skip this loop
			if ( candidateItems.size() == 0 ) {
				continue;
			}
			
			// for each candidate item, compare it to the camera we want
			for ( int i = 0; i < candidateItems.size(); i ++ ) {
				
				// prints out the candidate's name
				String candidateItem = (String)(candidateItems.elementAt(i));
				System.out.println("\nFound a candidate: " + candidateItem);
				currentCamera.clearAll();
			
				// find the pixel value
				String pixel = getPixel(ontModel,candidateItem);
				currentCamera.setPixel(pixel);
				
				// find lens:focalLength value
				String focalLength = getFocalLength(ontModel,candidateItem);
				currentCamera.setFocalLength(focalLength);
				show("focalLength",focalLength);
			
				// find lens:aperture value
				String aperture = getAperture(ontModel,candidateItem,MyShopBot.MIN);
				currentCamera.setMinAperture(aperture);
				show("min aperture",aperture);
				aperture = getAperture(ontModel,candidateItem,MyShopBot.MAX);
				currentCamera.setMaxAperture(aperture);
				show("max aperture",aperture);
			
				// find body:shutterSpeed value
				String shutterSpeed = getShutterSpeed(ontModel,candidateItem,MyShopBot.MIN);
				currentCamera.setMinShutterSpeed(shutterSpeed);
				show("min shutterSpeed",shutterSpeed);
				shutterSpeed = getShutterSpeed(ontModel,candidateItem,MyShopBot.MAX);
				currentCamera.setMaxShutterSpeed(shutterSpeed);
				show("max shutterSpeed",shutterSpeed);
				
				// if this candate camera is the one we want, print out we found a match
				if ( myCamera.sameAs(currentCamera) == true ) {
					System.out.println("found one match!");
				}
			}
				
			break;  // or next catalog file.
		}

	}

	// returns an array of candidate items
	private Vector findCandidateItem(Model m) {

		Vector candidates = new Vector();
		
		// SPARQL to query for candidate items
		String queryString =  
		    "SELECT ?candidate " +
			"WHERE {" +
			"   ?candidate <" + RDF.type + "> <" + targetType + ">. " +
			"   }"; 
		
		Query q = QueryFactory.create(queryString);
		QueryExecution qe = QueryExecutionFactory.create(q,m);
		ResultSet rs = qe.execSelect();
		
		// for each found matches, store it in the array.
		while ( rs.hasNext() ) {
			ResultBinding binding = (ResultBinding)rs.next();
			RDFNode rn = (RDFNode)binding.get("candidate");
			if ( rn != null && rn.isAnon() == false ) {
				candidates.add(rn.toString());
			} 
		}
		qe.close();
		return candidates;
	}

	// gets the pixel value from the model
	private String getPixel(Model m, String itemURI) {
		
		// SPARQL queary for searching for the pixel value
		String queryString =  
		    "SELECT ?value " +
			"WHERE {" +
			"   <" + itemURI + "> <http://www.liyangyu.com/camera#effectivePixel> ?value. " +
			"   }"; 
		
		// creates a query and queary execution object to search for values in the model
		Query q = QueryFactory.create(queryString);
		QueryExecution qe = QueryExecutionFactory.create(q,m);
		ResultSet rs = qe.execSelect(); // stores the results in the result set
		
		while ( rs.hasNext() ) {
			// checks each result and returns the string of the value if valid
			ResultBinding binding = (ResultBinding)rs.next();
			RDFNode rn = (RDFNode)binding.get("value");
			if ( rn != null && rn.isAnon() == false ) {
				return rn.toString();
			} 
		}
		qe.close();
		return null;
		
	}

	// gets the focal length from the model
	private String getFocalLength(Model m, String itemURI) {
		
		// query to search for the focal length in the model
		// is bit more complex then get pixel since its a chained queary
		String queryString =  
		    "SELECT ?value " +
			"WHERE {" +
			"   <" + itemURI + "> <http://www.liyangyu.com/camera#lens> ?tmpValue. " + // first it setns ?tmpValue as :Lens
			"   ?tmpValue <http://www.liyangyu.com/camera#focalLength> ?value . " + // then ?value will be the value of the focal length
			"   }"; 
		
		Query q = QueryFactory.create(queryString);
		QueryExecution qe = QueryExecutionFactory.create(q,m);
		ResultSet rs = qe.execSelect();
		
		while ( rs.hasNext() ) {
			ResultBinding binding = (ResultBinding)rs.next();
			RDFNode rn = (RDFNode)binding.get("value");
			if ( rn != null && rn.isAnon() == false ) {
				return rn.toString();
			} 
		}
		qe.close();
		return null;
		
	}
	
	// gets teh aperture from the model, min or max is determined based on the int minMaxFlag value
	private String getAperture(Model m, String itemURI, int minMaxFlag) {
		
		// Queary for min aperture
		String queryString = null;
		if ( minMaxFlag == this.MIN ) {
			queryString = 
				"SELECT ?value " + 
				"WHERE {" +
				"   <" + itemURI + "> <http://www.liyangyu.com/camera#lens> ?tmpValue0. " +
				"   ?tmpValue0 <http://www.liyangyu.com/camera#aperture> ?tmpValue1 . " +
				"   ?tmpValue1 <http://www.liyangyu.com/camera#minValue> ?value . " +
				"   }"; 
		} else {
			// Queary for max aperture
			queryString = 
				"SELECT ?value " + 
				"WHERE {" +
				"   <" + itemURI + "> <http://www.liyangyu.com/camera#lens> ?tmpValue0. " +
				"   ?tmpValue0 <http://www.liyangyu.com/camera#aperture> ?tmpValue1 . " +
				"   ?tmpValue1 <http://www.liyangyu.com/camera#maxValue> ?value . " +
				"   }";
		}
		    
		Query q = QueryFactory.create(queryString);
		QueryExecution qe = QueryExecutionFactory.create(q,m);
		ResultSet rs = qe.execSelect();
		
		String resultStr = "";
		while ( rs.hasNext() ) {
			ResultBinding binding = (ResultBinding)rs.next(); 
			RDFNode rn = (RDFNode)binding.get("value");
			if ( rn != null && rn.isAnon() == false ) {
				return rn.toString();
			} 
		}
		qe.close();
		return null;
		
	}
	
	// gets the shutter speed from the model, min or max is determined base on the minMaxFlag
	private String getShutterSpeed(Model m, String itemURI, int minMaxFlag) {
		
		// queary for min
		String queryString = null;
		if ( minMaxFlag == MyShopBot.MIN ) {
			queryString = 
				"SELECT ?value " + 
				"WHERE {" +
				"   <" + itemURI + "> <http://www.liyangyu.com/camera#body> ?tmpValue0. " +
				"   ?tmpValue0 <http://www.liyangyu.com/camera#shutterSpeed> ?tmpValue1 . " +
				"   ?tmpValue1 <http://www.liyangyu.com/camera#minValue> ?value . " +
				"   }"; 
		} else {
			// queary for max
			queryString = 
				"SELECT ?value " + 
				"WHERE {" +
				"   <" + itemURI + "> <http://www.liyangyu.com/camera#body> ?tmpValue0. " +
				"   ?tmpValue0 <http://www.liyangyu.com/camera#shutterSpeed> ?tmpValue1 . " +
				"   ?tmpValue1 <http://www.liyangyu.com/camera#maxValue> ?value . " +
				"   }";
		}
		
		Query q = QueryFactory.create(queryString);
		QueryExecution qe = QueryExecutionFactory.create(q,m);
		ResultSet rs = qe.execSelect();
		
		String resultStr = "";
		while ( rs.hasNext() ) {
			ResultBinding binding = (ResultBinding)rs.next();
		    RDFNode rn = (RDFNode)binding.get("value");
			if ( rn != null && rn.isAnon() == false ) {
				return rn.toString();
			} 
		}
		qe.close();
		return null;
		
	}
	
	// gets the item we want to search for in the model
	private boolean getItemToSearch(Model m) {
		
		String queryString =  
		    "SELECT ?subject ?predicate ?object " +
			"WHERE {" +
			"   ?subject <" + RDF.type + "> ?object. " + 
			"   }"; 

		Query q = QueryFactory.create(queryString);
		QueryExecution qe = QueryExecutionFactory.create(q,m);
		ResultSet rs = qe.execSelect();
		
		// collect the data type property names
		while ( rs.hasNext() ) {
			ResultBinding binding = (ResultBinding)rs.next();
			RDFNode rn = (RDFNode)binding.get("subject");
			if ( rn != null ) {
				targetItem = rn.toString();
			}
			rn = (RDFNode)binding.get("object");
			if ( rn != null ) {
				targetType = rn.toString();
			}
		}
		qe.close();
		
		if ( targetItem == null || targetItem.length() == 0 ) {
			return false;
		}
		if ( targetType == null || targetType.length() == 0 ) {
			return false;
		}
		return true;
	}

	// read in a given RDF document and creates a model
	private Model getModel(String rdfURL) {
		
		Model m = null;
		if ( rdfURL == null ) return m;
		
		try {
			// creates the model to work with
			m = ModelFactory.createDefaultModel();
			FileManager.get().readModel(m,rdfURL);
			// m.read(rdfURL);
		} catch (Exception e) {
			System.out.println("====> error reading foaf file at [" + rdfURL + "]");
			m = null;
		}
		return m;
	}
	
	// determines if the model contains what we are looking for
	private boolean isCandidate(Model m) {
		
		if ( m == null  ) {
			return false;
		}
		
		HashSet ns = new HashSet();
		this.collectNamespaces(m,ns);
		return ns.contains(ontologyNS); // checks the ontology matches the namespace
		
	}

	// stores the namespaces in the model in a hash set
	private void collectNamespaces(Model m,HashSet hs) {
		if ( hs == null || m == null ) {
			return;
		}
		NsIterator nsi = m.listNameSpaces();
		while ( nsi.hasNext() ) {
			hs.add(nsi.next().toString());
		}		
	}
	
	// for printing out values
	private void show(String t, String s) {
		if ( s != null && s.length() != 0 ) {
			System.out.println(t + " value is: " + s);
		} else {
			System.out.println(t + " value is not specified." );
		}
	}
	
}