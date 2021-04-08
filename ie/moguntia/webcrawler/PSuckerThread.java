package ie.moguntia.webcrawler;

import ie.moguntia.threads.*;
import java.net.*;
import java.io.*;
import java.util.Vector;
import java.util.Scanner;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.OutputStream;

//import org.jsoup.Jsoup;

import ie.moguntia.threads.ControllableThread;


public class PSuckerThread extends ControllableThread {
	public void process(Object o) {
		// The objects that we're dealing with here a strings for urls
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
			// if(filename.endsWith(".html")){
			// 		StringBuilder sb = new StringBuilder();
			//         for(Scanner sc = new Scanner(pageURL.openStream()); sc.hasNext(); )
			//         sb.append(sc.nextLine()).append('\n');
			//         String contents = sb.toString();
			//         contents = contents.replaceAll("<[^>]*>", "");
			//         contents = contents.replaceAll("\\{.*?\\}", "");
			//         contents = contents.replaceAll("\\s+"," ");
            //         BufferedWriter out = new BufferedWriter(
            //         new FileWriter(f1, true));
            //         out.write(contents);
            //         out.close();
		
			// 	}
			if (filename.endsWith(".jpg") ||
			   filename.endsWith(".png") ||
				filename.endsWith(".jpeg")||
				filename.endsWith(".mpeg") ||
				filename.endsWith(".mpg") ||
				filename.endsWith(".avi") ||
				filename.endsWith(".wmv") ||
				filename.endsWith(".pdf") ||
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

			// If it's neither a jpg nor some text, it's not interesting.
 			String mimetype = pageURL.openConnection().getContentType();
            if (!mimetype.startsWith("text")) return;

			String rawPage = SaveURL.getURL(pageURL);
            String smallPage = rawPage.toLowerCase().replaceAll("\\s", " ");
			// treat the url a a html file and try to extract links
			Vector links = SaveURL.extractLinks(rawPage, smallPage);
			// Convert each link text to a url and enque
			for (int n = 0; n < links.size(); n++) {
				try {
					// urls might be relative to current page
					URL link = new URL(pageURL,
									   (String) links.elementAt(n));
					// If layers are not used, write everything into same layer
					if (tc.getMaxLevel() == -1)
						queue.push(link, level);
					else
						queue.push(link, level + 1);
				} catch (MalformedURLException e) {
					// Ignore malformed URLs, the link extractor might
					// have failed.
				}
			}
		} catch (Exception e) {
			// e.printStackTrace();
			// process of this object has failed, but we just ignore it here
		}
	}
}
