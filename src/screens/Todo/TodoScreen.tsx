import React, { useState } from 'react';
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  ScrollView,
  StyleSheet,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { colors, spacing, typography, globalStyles } from '../../styles/theme';

type Todo = {
  id: string;
  content: string;
  completed: boolean;
  date: string;
};

export const TodoScreen: React.FC = () => {
  const [content, setContent] = useState('');
  const [todos, setTodos] = useState<Todo[]>([]);
  const [editingId, setEditingId] = useState<string | null>(null);

  const addTodo = () => {
    if (!content.trim()) return;

    if (editingId) {
      setTodos(todos.map(todo =>
        todo.id === editingId
          ? { ...todo, content }
          : todo
      ));
      setEditingId(null);
    } else {
      const newTodo: Todo = {
        id: Date.now().toString(),
        content,
        completed: false,
        date: new Date().toLocaleDateString(),
      };
      setTodos([newTodo, ...todos]);
    }
    setContent('');
  };

  const toggleTodo = (id: string) => {
    setTodos(todos.map(todo =>
      todo.id === id
        ? { ...todo, completed: !todo.completed }
        : todo
    ));
  };

  const editTodo = (todo: Todo) => {
    setContent(todo.content);
    setEditingId(todo.id);
  };

  const deleteTodo = (id: string) => {
    setTodos(todos.filter(todo => todo.id !== id));
    if (editingId === id) {
      setEditingId(null);
      setContent('');
    }
  };

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView style={styles.content}>
        <View style={styles.inputContainer}>
          <View style={styles.inputRow}>
            <TextInput
              style={styles.input}
              value={content}
              onChangeText={setContent}
              placeholder="添加待办事项..."
            />
            <TouchableOpacity
              style={styles.addButton}
              onPress={addTodo}
            >
              <Text style={styles.addButtonText}>
                {editingId ? '✓' : '+'}
              </Text>
            </TouchableOpacity>
          </View>
        </View>

        <View style={styles.todosContainer}>
          <Text style={styles.title}>待办事项列表</Text>
          {todos.map((todo) => (
            <View key={todo.id} style={styles.todoCard}>
              <TouchableOpacity
                style={styles.checkbox}
                onPress={() => toggleTodo(todo.id)}
              >
                {todo.completed && <View style={styles.checked} />}
              </TouchableOpacity>
              <View style={styles.todoContent}>
                <Text style={[
                  styles.todoText,
                  todo.completed && styles.completedText
                ]}>
                  {todo.content}
                </Text>
                <Text style={styles.date}>{todo.date}</Text>
              </View>
              <View style={styles.todoActions}>
                <TouchableOpacity
                  style={[styles.actionButton, styles.editButton]}
                  onPress={() => editTodo(todo)}
                >
                  <Text style={styles.actionButtonText}>编辑</Text>
                </TouchableOpacity>
                <TouchableOpacity
                  style={[styles.actionButton, styles.deleteButton]}
                  onPress={() => deleteTodo(todo.id)}
                >
                  <Text style={[styles.actionButtonText, styles.deleteButtonText]}>
                    删除
                  </Text>
                </TouchableOpacity>
              </View>
            </View>
          ))}
        </View>
      </ScrollView>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: {
    ...globalStyles.container,
  },
  content: {
    flex: 1,
    padding: spacing.md,
  },
  inputContainer: {
    marginBottom: spacing.lg,
  },
  inputRow: {
    flexDirection: 'row',
    gap: spacing.md,
  },
  input: {
    ...globalStyles.input,
    flex: 1,
  },
  addButton: {
    ...globalStyles.button,
    width: 40,
    height: 40,
    padding: 0,
  },
  addButtonText: {
    ...globalStyles.buttonText,
    fontSize: typography.sizes.large,
  },
  todosContainer: {
    gap: spacing.md,
  },
  title: {
    ...globalStyles.title,
  },
  todoCard: {
    ...globalStyles.card,
    flexDirection: 'row',
    alignItems: 'center',
    gap: spacing.md,
  },
  checkbox: {
    width: 24,
    height: 24,
    borderRadius: 12,
    borderWidth: 2,
    borderColor: colors.border,
    alignItems: 'center',
    justifyContent: 'center',
  },
  checked: {
    width: 12,
    height: 12,
    borderRadius: 6,
    backgroundColor: colors.success,
  },
  todoContent: {
    flex: 1,
  },
  todoText: {
    fontSize: typography.sizes.regular,
    color: colors.text,
    marginBottom: spacing.xs,
  },
  completedText: {
    textDecorationLine: 'line-through',
    color: colors.textLight,
  },
  date: {
    fontSize: typography.sizes.small,
    color: colors.textLight,
  },
  todoActions: {
    flexDirection: 'row',
    gap: spacing.sm,
  },
  actionButton: {
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.sm,
    borderRadius: 4,
  },
  actionButtonText: {
    fontSize: typography.sizes.small,
    color: colors.text,
  },
  editButton: {
    backgroundColor: colors.secondary,
  },
  deleteButton: {
    backgroundColor: colors.danger + '20',
  },
  deleteButtonText: {
    color: colors.danger,
  },
}); 