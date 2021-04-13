package os.project.webcrawler;

import java.net.*;
import java.util.Vector;

import os.project.threads.*;

import java.util.Scanner;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;


public class PSuckerThread extends ControllableThread {
	public void process(Object o) {
		try {

			File f1 = new File("data.txt");
            if(!f1.exists()) {
            f1.createNewFile();
         }

			URL pageURL = (URL) o;
            

              
			String filename = pageURL.getFile().toLowerCase();
			String mime=pageURL.openConnection().getContentType();
			if (mime.startsWith("text") || filename.endsWith(".html") ){
				StringBuilder sb = new StringBuilder();
				for(Scanner sc = new Scanner(pageURL.openStream()); sc.hasNext(); )
				sb.append(sc.nextLine()).append('\n');
				String contents = sb.toString();
				contents = contents.replaceAll("<script[^<]*</script>", "");
				contents = contents.replaceAll("<style[^<]*</style>", "");
				contents = contents.replaceAll("<[^>]*>", "");
				contents = contents.replaceAll("(?m)(^\\s+|[\\t\\f ](?=[\\t\\f ])|[\\t\\f ]$|\\s+\\z)", "");
				BufferedWriter out = new BufferedWriter(
				new FileWriter(f1, true));
				out.write(contents);
				out.close();
			}
		
			if (filename.endsWith(".jpg") ||
			    filename.endsWith(".html")||
				filename.endsWith(".xml")||
				filename.endsWith(".css")||
				filename.endsWith(".js")||
			    filename.endsWith(".png") ||
				filename.endsWith(".jpeg")||
				filename.endsWith(".mpeg") ||
				filename.endsWith(".mpg") ||
				filename.endsWith(".avi") ||
				filename.endsWith(".wmv") ||
				filename.endsWith(".pdf") ||
				filename.endsWith(".mp3") ||
				filename.endsWith(".mp4") ||
				filename.endsWith(".mov") ||
				filename.endsWith(".mpg") ||
				filename.endsWith(".wevm") ||
				filename.endsWith(".mpv") ||
				filename.endsWith(".txt")) {
				filename = filename.replace('/', '-');
				filename = ((URLQueue) queue).getFilenamePrefix() +
					pageURL.getHost() + filename;
				System.out.println("Saving to file " + filename);

				try {
					SaveURL.writeURLtoFile(pageURL,filename);
				} catch (Exception e) {
					System.out.println("Saving to file " + filename + " from URL " + pageURL.toString() + " failed due to a " + e.toString());
				}
				return;
			}

 			String mimetype = pageURL.openConnection().getContentType();
            if (!mimetype.startsWith("text")) return;

			String rawPage = SaveURL.getURL(pageURL);
            String smallPage = rawPage.toLowerCase().replaceAll("\\s", " ");
			Vector links = SaveURL.extractLinks(rawPage, smallPage);
			for (int n = 0; n < links.size(); n++) {
				try {
					URL link = new URL(pageURL,
									   (String) links.elementAt(n));
					if (tc.getMaxLevel() == -1)
						queue.push(link, level);
					else
						queue.push(link, level + 1);
				} catch (MalformedURLException e) {
				}
			}
		} catch (Exception e) {
		}
	}
}
