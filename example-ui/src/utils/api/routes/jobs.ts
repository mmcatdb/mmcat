import type { Empty, StringLike } from "@/types/api/routes";
import { GET, POST, DELETE } from "../routeFunctions";
import type { JobFromServer, JobInit } from "@/types/job";

const jobs = {
    getAllJobsInCategory: GET<{ categoryId: StringLike }, JobFromServer[]>(
        u => `/schema-categories/${u.categoryId}/jobs`
    ),
    getJob: GET<{ id: StringLike }, JobFromServer>(
        u => `/jobs/${u.id}`
    ),
    createNewJob: POST<Empty, JobFromServer, JobInit>(
        () => `/jobs`
    ),
    startJob: POST<{ id: StringLike }, JobFromServer>(
        u => `/jobs/${u.id}/start`
    ),
    deleteJob: DELETE<{ id: StringLike }, void>(
        u => `/jobs/${u.id}`
    )
};

export default jobs;
