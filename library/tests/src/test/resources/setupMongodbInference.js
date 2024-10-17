db.dropDatabase();

db.business.insertMany([
    {
        business_id: "mpf3x-BjTdTEA3yCZrAYPw",
        name: "The UPS Store",
        address: "87 Grasso Plaza Shopping Center",
        city: "Affton",
        state: "MO",
        postal_code: "63123",
        stars: 3.0,
        review_count: 15,
        is_open: 1,
        attributes: {
            BusinessAcceptsCreditCards: "True",
            HasTV: "False",
        },
        categories: "Shipping Centers, Local Services, Notaries, Mailbox Centers, Printing Services",
        hours: {
            Monday: "0:0-0:0",
            Tuesday: "8:0-18:30",
            Wednesday: "8:0-18:30",
            Thursday: "8:0-18:30",
            Friday: "8:0-18:30",
            Saturday: "8:0-14:0",
        },
    },
    {
        business_id: "MTSW4McQd7CbcVtyjqoe9mw",
        name: "St Honore Pastries",
        address: "935 Race St",
        city: "Philadelphia",
        state: "PA",
        postal_code: "19107",
        stars: 4.0,
        review_count: 80,
        is_open: 1,
        attributes: {
            BusinessAcceptsCreditCards: "False",
            HasTV: "True",
        },
        categories: "Restaurants, Food, Bubble Tea, Coffee & Tea, Bakeries",
        hours: {
            Monday: "7:0-20:0",
            Tuesday: "7:0-20:0",
            Wednesday: "7:0-20:0",
            Thursday: "7:0-20:0",
            Friday: "7:0-21:0",
            Saturday: "7:0-21:0",
        },
    },
]);
