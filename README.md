# JobML

The purpose of this machine learning engine is to match a set of sentences with the reviews of a company
This set of sentences written by the user is compared to the reviews scrapped from glassdoor and other sites
I then use a machine learning algorithm set to determine the best match company between what the user wants and what people say about a company

For example:

If a user wants to work at:
"A place I really want to work is California. I want to make a lot of money and I want to be challenged. 
I like social media and cameras. I like the startup enviornment and culture so a company where I am free to do as I please is important. 
I want young people and great pay. Prefrebaly something that will feel very college. I like jets"

They get matched with snapchat

But if a person states:

"I like government and jets and not have to work all of January because of government shutdowns"

They get matched with Lockheed Martin

The safistication of this project can be exponential and the uses for an engine as such can be put to almost anything.
I find it best fit to a job matcher as it is relevent to students trying to find a job that best suits them based on
work environment, culture, easiness, pay, type of work etc. Also we don't need to preset and categorize companies, the
machine learning algorithm figures it all out on its own, all we need to do is scrape data from review sites such as glassdoor.

To run:

Call main function with arguments "paragraph" "username" and the output shoud catogorize the parahraph under a company and store in mongo
