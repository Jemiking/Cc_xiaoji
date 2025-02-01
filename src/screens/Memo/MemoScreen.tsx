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

type Memo = {
  id: string;
  content: string;
  date: string;
};

export const MemoScreen: React.FC = () => {
  const [content, setContent] = useState('');
  const [memos, setMemos] = useState<Memo[]>([]);
  const [editingId, setEditingId] = useState<string | null>(null);

  const saveMemo = () => {
    if (!content.trim()) return;

    if (editingId) {
      setMemos(memos.map(memo =>
        memo.id === editingId
          ? { ...memo, content, date: new Date().toLocaleDateString() }
          : memo
      ));
      setEditingId(null);
    } else {
      const newMemo: Memo = {
        id: Date.now().toString(),
        content,
        date: new Date().toLocaleDateString(),
      };
      setMemos([newMemo, ...memos]);
    }
    setContent('');
  };

  const editMemo = (memo: Memo) => {
    setContent(memo.content);
    setEditingId(memo.id);
  };

  const deleteMemo = (id: string) => {
    setMemos(memos.filter(memo => memo.id !== id));
    if (editingId === id) {
      setEditingId(null);
      setContent('');
    }
  };

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView style={styles.content}>
        <View style={styles.inputContainer}>
          <TextInput
            style={[styles.input, styles.memoInput]}
            value={content}
            onChangeText={setContent}
            placeholder="写下您的备忘录..."
            multiline
          />
          <TouchableOpacity
            style={styles.button}
            onPress={saveMemo}
          >
            <Text style={styles.buttonText}>
              {editingId ? '保存修改' : '保存'}
            </Text>
          </TouchableOpacity>
        </View>

        <View style={styles.memosContainer}>
          <Text style={styles.title}>备忘录列表</Text>
          {memos.map((memo) => (
            <View key={memo.id} style={styles.memoCard}>
              <View style={styles.memoContent}>
                <Text style={styles.memoText}>{memo.content}</Text>
                <Text style={styles.date}>{memo.date}</Text>
              </View>
              <View style={styles.memoActions}>
                <TouchableOpacity
                  style={[styles.actionButton, styles.editButton]}
                  onPress={() => editMemo(memo)}
                >
                  <Text style={styles.actionButtonText}>编辑</Text>
                </TouchableOpacity>
                <TouchableOpacity
                  style={[styles.actionButton, styles.deleteButton]}
                  onPress={() => deleteMemo(memo.id)}
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
    gap: spacing.md,
    marginBottom: spacing.lg,
  },
  input: {
    ...globalStyles.input,
  },
  memoInput: {
    height: 100,
    textAlignVertical: 'top',
    paddingTop: spacing.md,
  },
  button: {
    ...globalStyles.button,
  },
  buttonText: {
    ...globalStyles.buttonText,
  },
  memosContainer: {
    gap: spacing.md,
  },
  title: {
    ...globalStyles.title,
  },
  memoCard: {
    ...globalStyles.card,
  },
  memoContent: {
    flex: 1,
    marginBottom: spacing.md,
  },
  memoText: {
    fontSize: typography.sizes.regular,
    color: colors.text,
    marginBottom: spacing.sm,
  },
  date: {
    fontSize: typography.sizes.small,
    color: colors.textLight,
  },
  memoActions: {
    flexDirection: 'row',
    justifyContent: 'flex-end',
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