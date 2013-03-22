package com.tegik.facelectr.ad_actionButton;

import org.xml.sax.*;
import org.xml.sax.helpers.*;
import java.io.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.FileWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import java.io.InputStreamReader;
import java.io.BufferedReader;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.PrintStream;
import java.io.File;

import java.util.Date;
import java.util.*;
import java.util.Vector;

public class extraeTimbrado extends DefaultHandler {

//Inicializacion de variables para sacar el timbrado del archivo xml
private static String Fechatimbrado = "";
private static String uuid = "";
private static String noCertificadoSAT = "";
private static String selloCFD = "";
private static String selloSAT = "";
private static String version = "";

private static String Fechatimbrado_atributo = "";
private static String uuid_atributo = "";
private static String noCertificadoSAT_atributo = "";
private static String selloCFD_atributo = "";
private static String selloSAT_atributo = "";
private static String version_atributo = "";



private static String CadenaOriginalTimbrado = "";

   public void startElement( String namespaceURI, String localName,String qName, Attributes attr ) throws SAXException  {

	//Obtinene los nombre de atributos y los atributos del archivo timbrado y los guarda en la variable
        Fechatimbrado= attr.getLocalName(0) + "=\"" +  attr.getValue(0)+"\"";
	uuid= attr.getLocalName(1) + "=\"" +  attr.getValue(1)+"\"";
	noCertificadoSAT= attr.getLocalName(2) + "=\"" +  attr.getValue(2)+"\"";
	selloCFD= attr.getLocalName(3) + "=\"" +  attr.getValue(3)+"\"";
	selloSAT= attr.getLocalName(4) + "=\"" +  attr.getValue(4)+"\"";
	version= attr.getLocalName(5) + "=\"" +  attr.getValue(5)+"\"";

	//Obtinene los atributos del archivo timbrado y los guarda en la variable
	Fechatimbrado_atributo = attr.getValue(0);
	uuid_atributo = attr.getValue(1);
	noCertificadoSAT_atributo = attr.getValue(2);
	selloCFD_atributo = attr.getValue(3);
	selloSAT_atributo = attr.getValue(4);
	version_atributo = attr.getValue(5);
   }

   public static void timbra(String ruta, String NumFac) throws IOException{

	      //Eliminar la ultima linea porque se va a recorrer al final </cfdi:Comprobante> y asi poder meter los datos que se reciberon del PAC
	      Vector lineas=new Vector();
	      FileReader contarlineas = new FileReader (ruta+NumFac+".xml"); //Abre el archivo de la factura original para leer
	      BufferedReader br = new BufferedReader(contarlineas);
	      String linea;       
	      int cont=0;
	      while((linea=br.readLine())!=null){	//Cuenta cuantas lineas tiene el archivo y las guarda en el contador
	      cont++;
	      }
	      br.close(); //Se cierra el archivo


	      FileReader leerarchivo = new FileReader (ruta+NumFac+".xml"); //Abre el archivo de la factura original para escribir
	      BufferedReader la = new BufferedReader(leerarchivo);
	      int n=cont;	//n es igual al numero de lineas que tiene la factura original
	      cont=0;
	      while((linea=la.readLine())!=null){	//Se recorre el archivo leyendo las lineas
	      cont++;
	      if(cont!=n)		//Si el contador es diferente a la linea, se agrega la linea al arreglo de elementos
	      lineas.addElement(linea);
	      }
	      la.close(); 	//Se cierra el archivo


	      FileWriter fichero = new FileWriter(ruta+NumFac+".xml");	//Abre el archivo de la factura original para escribir
	      PrintWriter escribe = new PrintWriter(fichero);
	      for(int i=0;i<lineas.size();i++)
	      escribe.println(lineas.elementAt(i));	//Escribe las lienas que tenia en el arreglo de elementos
	      fichero.close();//Se cierra el archivo
	      //Termina: Eliminar la ultima linea porque se va a recorrer al final </cfdi:Comprobante>


      try {
	  String namespaceURI;
	  String localName;
	  String qName;
	  Attributes attr;

         // Create SAX 2 parser...
         XMLReader xr = XMLReaderFactory.createXMLReader();

         // Set the ContentHandler...
         xr.setContentHandler(new extraeTimbrado());

            // Parse the file...
         xr.parse(new InputSource(new FileReader(ruta+"Timbrado"+NumFac+".xml")));

	OutputStreamWriter escribetimbrado = new OutputStreamWriter(new FileOutputStream(ruta+NumFac+".xml", true), "UTF-8");//Abre el archivo de Factura Original para escribir los datos del timbrado
	BufferedWriter entradatimbrado = new BufferedWriter(escribetimbrado);
	//Se agrega el codigo xml del timbrado a la factura original
	entradatimbrado.write("    <cfdi:Complemento>\n");
	entradatimbrado.write("    <tfd:TimbreFiscalDigital\n");
	entradatimbrado.write("    "+selloSAT+"\n");
	entradatimbrado.write("    "+noCertificadoSAT+"\n");
	entradatimbrado.write("    "+selloCFD+"\n"); //El sello ya aparece antes con el nombre sello en la factura original
	entradatimbrado.write("    "+Fechatimbrado);
	entradatimbrado.write("  "+uuid);
	entradatimbrado.write("  "+version+"/>\n");
	entradatimbrado.write("    </cfdi:Complemento>\n");
	entradatimbrado.write("</cfdi:Comprobante>\n");
	entradatimbrado.close();
	//Termian Se agrega el codigo xml del timbrado a la factura original

      CadenaOriginalTimbrado = "ǀǀ"+version_atributo+"ǀ"+uuid_atributo+"ǀ"+Fechatimbrado_atributo+"ǀ"+selloSAT_atributo+"ǀ"+noCertificadoSAT_atributo+"ǀǀ";

      }catch ( Exception e ) {
         e.printStackTrace();
      }

   }

    public static String get_selloSAT(){
    return selloSAT_atributo;
    }

    public static String get_noCertificadoSAT(){
    return noCertificadoSAT_atributo;
    }

    public static String get_selloCFD(){
    return selloCFD_atributo;
    }

    public static String get_Fechatimbrado(){
    return Fechatimbrado_atributo;
    }

    public static String get_uuid(){
    return uuid_atributo;
    }

    public static String get_version(){
    return version_atributo;
    }

    public static String get_CadenaOriginalTimbrado(){
    return CadenaOriginalTimbrado;
    }



}