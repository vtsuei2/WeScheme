package org.wescheme.project;

import java.io.Serializable;
import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.jdom.Element;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Text;

import org.wescheme.data.History;
import org.wescheme.util.Base64;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class HistoryEntry implements Serializable {
		private	 static final long serialVersionUID = -2L; //TODO: generate
		
		@PrimaryKey
		@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
		protected Key key;
		
		@Persistent
		protected Text command;
		
		@Persistent
		protected long time;
		
		public HistoryEntry(String com,  long time) {
			this.command = new Text(com);
			this.time = time;
		}
		
		public String getCommand() {
			return this.command.getValue();
		}
		
		public long getTime() {
			return this.time;
		}
		
		public String toString() {
			return this.command.getValue();
		}
		
		public Element toXML() {
			Element root = new Element("history-entry");
			root.setText(Base64.encodeBytes(command.getValue().getBytes()));
			return root;
		}
		
		public JSONArray toJSON() {
			JSONArray jsonArr = new JSONArray();
			jsonArr.add(this.command.getValue());
			jsonArr.add(this.time);
			return jsonArr;
			//JSONObject json = new JSONObject();
			//json.put("history-entry", this.command.getValue());
			//json.put("date",  this.time);
			//return json;
		}
}
