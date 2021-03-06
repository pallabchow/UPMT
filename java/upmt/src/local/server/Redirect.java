/*
 * Copyright (C) 2005 Luca Veltri - University of Parma - Italy
 * 
 * This source code is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this source code; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * Author(s):
 * Luca Veltri (luca.veltri@unipr.it)
 */

package local.server;


import java.util.Vector;

import org.zoolu.sip.header.MultipleHeader;
import org.zoolu.sip.header.SipHeaders;
import org.zoolu.sip.message.Message;
import org.zoolu.sip.message.MessageFactory;
import org.zoolu.sip.provider.SipProvider;
import org.zoolu.sip.provider.SipStack;
import org.zoolu.tools.Log;


/** Class Redirect implement a SIP edirect server.
  * It extends class Registrar. A Redirect can work as simply SIP redirect,
  * or it can handle calls for registered users. 
  */
public class Redirect extends Registrar
{   
   /** Costructs a new Redirect that acts also as location server for registered users. */
   public Redirect(SipProvider provider, ServerProfile server_profile)
   {  super(provider,server_profile);
   }
      
   /** When a new request message is received for a local user */
   public void processRequestToLocalUser(Message msg)
   {  printLog("inside processRequestToLocalUser(msg)",Log.LEVEL_MEDIUM);
      
      // message targets
      Vector contacts=getTargets(msg);

      if (contacts.isEmpty())
      {  printLog("No target found, message discarded",Log.LEVEL_HIGH);
         if (!msg.isAck()) sip_provider.sendMessage(MessageFactory.createResponse(msg,404,null,null));
         return;
      } 
                
      printLog("message will be redirect to all user's contacts",Log.LEVEL_MEDIUM);         
      // create the response with all contact urls, and send it 
      MultipleHeader mc=new MultipleHeader(SipHeaders.Contact,contacts);
      mc.setCommaSeparated(true);
      Message resp=MessageFactory.createResponse(msg,302,null,null);
      resp.setContacts(mc);
      sip_provider.sendMessage(resp);      
   }
   
   /** When a new request message is received for a remote UA */
   public void processRequestToRemoteUA(Message msg)
   {  printLog("inside processRequestToRemoteUA(msg)",Log.LEVEL_MEDIUM);
      printLog("request not for local server",Log.LEVEL_HIGH);
      if (!msg.isAck()) sip_provider.sendMessage(MessageFactory.createResponse(msg,404,null,null));
      else printLog("message discarded",Log.LEVEL_HIGH);
   }   

   /** When a new response message is received */
   public void processResponse(Message resp)
   {  printLog("inside processResponse(msg)",Log.LEVEL_MEDIUM);
      printLog("request not for local server: message discarded",Log.LEVEL_HIGH);
   }
   

 
   // ****************************** Logs *****************************

   /** Adds a new string to the default Log */
   private void printLog(String str, int level)
   {  if (log!=null) log.println("Redirect: "+str,ServerEngine.LOG_OFFSET+level);  
   }


   // ****************************** MAIN *****************************

   /** The main method. */
   public static void main(String[] args)
   {  
         
      String file=null;
      
      for (int i=0; i<args.length; i++)
      {  if (args[i].equals("-f") && args.length>(i+1))
         {  file=args[++i];
            continue;
         }
         if (args[i].equals("-h"))
         {  System.out.println("usage:\n   java Redirect [-f <config_file>] \n");
            System.exit(0);
         }
      }
                  
      SipStack.init(file);
      SipProvider sip_provider=new SipProvider(file);
      ServerProfile server_profile=new ServerProfile(file);

      new Redirect(sip_provider,server_profile);      
   }
  
}
