In this wave you will work with your partner to identify areas your code can be improved.

Make a file called humanImprovements.md. You will complete this file WITHOUT AI. Find the issues WITHOUT AI and write the file WITHOUT AI.

Work with your partner to review your code. Actually go back and look at all your files again! Take a look at your server code/tests, java code/tests, package.json, and pom.xml. Identify ways your code could be improved. As you go through, answer the below questions. You do not need to write full sentences, bullet points are fine. Again, do this WITHOUT AI. Make commits as you go. Spend a good deal of time on this!

refactoring code
What improvements can you make to the design/architecture of your code?
-Create YAML infrastructure files to support production PostgreSQL.
-Refactor to integrate ffmpeg for faster performance but keep existing CLI arguments.
- Create VideoControllerTest, VideoApiIntegrationTest, ThumbnailServiceTest with comprehensive unit tests
- Add Javadocs to any method in any class currently missing them.
- Remove any duplicate files from the repo
How can you split up large methods or classes into smaller components?
Are there unused files/methods that can be removed?
Where would additional Java interfaces be appropriate?
How can you make things simpler, more-usable, and easier to maintain?
Other refactoring improvements?
adding tests
What portions of your code are untested / only lightly tested?
Where would be the highest priority places to add new tests?
Other testing improvements?
improving error handling
What parts of your code are brittle?
Where could you better be using exceptions?
Where can you better add input validation to check invalid input?
How can you better be resolving/logging/surfacing errors? Hint: almost any place you're using "throws Exception" or "catch(Exception e)" should likely be improved to specify the specific types of exceptions that might be thrown or caught.
Other error handling improvements?
writing documentation
What portions of your code are missing Javadoc/JSdoc for the methods/classes?
What documentation could be made clearer or improved?
Are there sections of dead code that are commented out?
Where would be the most important places to add documentation to make your code easier to read?
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