db.createUser(
		{
			user: "contos",
			pwd: "33coherentA",
			roles: [
				{role: "readWrite", db: "Species"},
				{role: "readWrite", db: "Protocols"}
			]
		}
);

db.createUser(
		{
			user: "contos",
			pwd: "33coherentA",
			roles: [
				{role: "readWrite", db: "articles"},
				{role: "readWrite", db: "Species"},
				{role: "readWrite", db: "Protocols"},
				{role: "readWrite", db: "experiment"},
				{role: "readWrite", db: "experiment1"},
				{role: "readWrite", db: "experiment2"},
				{role: "readWrite", db: "experiment3"},
				{role: "readWrite", db: "experiment4"},
				{role: "readWrite", db: "experiment5"},
				{role: "readWrite", db: "experiment6"},
				{role: "readWrite", db: "experiment7"},
				{role: "readWrite", db: "experiment8"},
				{role: "readWrite", db: "experiment9"}
			]
		}
);