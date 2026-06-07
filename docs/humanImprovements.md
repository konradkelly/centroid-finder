In this wave you will work with your partner to identify areas your code can be improved.

Make a file called humanImprovements.md. You will complete this file WITHOUT AI. Find the issues WITHOUT AI and write the file WITHOUT AI.

Work with your partner to review your code. Actually go back and look at all your files again! Take a look at your server code/tests, java code/tests, package.json, and pom.xml. Identify ways your code could be improved. As you go through, answer the below questions. You do not need to write full sentences, bullet points are fine. Again, do this WITHOUT AI. Make commits as you go. Spend a good deal of time on this!

refactoring code
What improvements can you make to the design/architecture of your code?

-Refactor to integrate ffmpeg for faster performance but keep existing CLI arguments.
- Create VideoControllerTest, VideoApiIntegrationTest, ThumbnailServiceTest with comprehensive unit tests
- Add Javadocs to any method in any class currently missing them.
- Improve error handling by using specific types of exceptions (eg., IOException).
- Use detailed and specific error messages from Job Service in error messages in the VideoProcessor app.
- Remove any duplicate files from the repo
-Create production branch with docker-compose.yml and Spring profile for PostgreSQL deployment.

How can you split up large methods or classes into smaller components?

They are already quite modular.

Are there unused files/methods that can be removed?

The JCodecExperiment App seems to be of no use at this point since it was created before the server branch. It should either be removed or reworked into a test file.

Where would additional Java interfaces be appropriate?

At this point, the existing interfaces appear sufficient and map logically to concrete implementations. 

How can you make things simpler, more-usable, and easier to maintain?
Other refactoring improvements?

Add more Java docs and tests.

adding tests
What portions of your code are untested / only lightly tested?

StaticFileConfig, VideoProcessingConfig, ServerPathsProperties have no tests as two of the are records and a configuration for the Spring API. The tests are are written are for core logic of this application. One class that could be tested is the JobStatusMapper to validate its mapping logic.

Where would be the highest priority places to add new tests?

APIExceptionHandler is missing tests in VideoControllerTest for when a video is not found or there is a server error.

Other testing improvements?
Production branch needs test against a DB instead of local file system. PostgreSQL Docker containers can be used.

improving error handling
What parts of your code are brittle?
I think the imagesummaryapp.java can be improved by using more specific exceptios  like a numberformatexception instead of the general ones that we have

Where could you better be using exceptions?
i think in jecodeexpremimentapp we can use different type of exceptions than using the general exceptions

Where can you better add input validation to check invalid input?
I'd say the ditanceimagebinarizer.java would improve with having a something valudate the threshold is not negative before processing 

How can you better be resolving/logging/surfacing errors? Hint: almost any place you're using "throws Exception" or "catch(Exception e)" should likely be improved to specify the specific types of exceptions that might be thrown or caught.
I'd say having / adding more validations to check if image exists, and if the color jas processed or if there are threshold ranges

Other error handling improvements?

writing documentation
What portions of your code are missing Javadoc/JSdoc for the methods/classes?
I'd say the toBinarryArray and toBuffered image can use javadoc comnments

What documentation could be made clearer or improved?
Some of the notes can be moved into javadoc comments, so its closer to the actual code

Are there sections of dead code that are commented out?
Not any that i know
Where would be the most important places to add documentation to make your code easier to read?
Notes.md -  we should review if the contents should be moved to javadoc comments

Other documentation improvements?
improving performance (optional)
What parts of your code / tests run particularly slowly?
What speed improvements would most make running / maintaining your code better?
Other performance improvements?
hardening security (optional)
What packages / images are out of date / have security issues?
Where could you have better input validation in your code to prevent malicious use?
Other security improvements?
bug fixes (optional)
What bugs do you know exist?
What parts of the code do you think might be causing them?
Other bug fix improvements?
other
Any other improvements in general you could make?