package ru.kanban.dao;

import java.io.IOException;

class InMemoryTaskDaoTest extends DaoTest {
    @Override
    TaskDao createDao() throws IOException {
        return new InMemoryTaskDao();
    }
}