

# JsIntent

Enable launch intent from javascript


```js.js

 - JsIntent

 	- jsIntent.launchEditSite(
		editPath: String,
		srcPath: String,
		onClickSort: String(true/false),
		onSortableJs: String(true/false),
		onClickUrl: String(true/false),
		filterCode: String,
		onDialog: String(true/false)
	  )
		- ref: [html automaticaly creation command to edit target edit file]

 	- jsIntent.launchUrl(
		urlString: String
          )
		-> launch uri(not url but uri)

	- jsIntent.launchApp(
		action: String,
		uriString: String,
		extraString: tabSepalatedString,
		extraInt: tabSepalatedString,
		extraLong: tabSepalatedString,
		extraFloat: tabSepalatedString
	   )
		- launch app site

		ex) bellow, launch google calendar  
			jsIntent.launchApp(
				"android.intent.action.INSERT",
				"content://com.android.calendar/events",
				"title=title\tdescription=description\teventLocation=eventLocation\tandroid.intent.extra.EMAIL=email",
				"",
				beginTime=167889547868058\tendTime=165678973498789",
				""
			);

	- jsIntent.launchShortcut(
		currentAppDirPath: String,
		currentShellFileName: String
	    )
		- launch index and fannel  

	- jsIntent.shareImage(
		shareImageFilePath: String
	  )
		- share image intent


```